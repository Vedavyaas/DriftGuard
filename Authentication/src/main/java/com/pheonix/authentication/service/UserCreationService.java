package com.pheonix.authentication.service;

import com.pheonix.authentication.assets.*;
import com.pheonix.authentication.repository.UserEntity;
import com.pheonix.authentication.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UserCreationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    public UserCreationService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    public JWTToken authenticate(LoginCredentials loginCredentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginCredentials.username(), loginCredentials.password())
        );

        Instant now = Instant.now();

        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();

        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();

        // Fetch role from DB to include in response (non-sensitive metadata)
        Role userRole = userRepository.findByUsername(authentication.getName())
                .map(UserEntity::getRole)
                .orElse(null);

        return new JWTToken(tokenValue, authentication.getName(), userRole);
    }

    public String createProjectManager(ProjectManagerCredentials credentials) {
        if (userRepository.existsByUsername(credentials.username())) {
            throw new DuplicateCredentialsException("Project Manager with given name already found!");
        }

        if (userRepository.existsByEmail(credentials.email())) {
            throw new DuplicateCredentialsException("Project Manager with given email already found!");
        }

        // Fix: constructor is (username, email, password, role, lastUpdatedAt)
        UserEntity user = new UserEntity(credentials.username(), credentials.email(), passwordEncoder.encode(credentials.password()), Role.PROJECTMANAGER, Instant.now());
        userRepository.save(user);

        return "Project Manager account created successfully";
    }

    public List<ProjectManagerDTO> getProjectManagers() {
        return userRepository.findAllByRole(Role.PROJECTMANAGER);
    }

    public ProjectManagerDTO getSelfInfo(String username) {
        if (!userRepository.existsByUsername(username)) {
            throw new DuplicateCredentialsException("Not found!");
        }

        return userRepository.findProjectedByUsername(username);
    }

    public String changeProjectManagerValidity(Long id, boolean validity) {
        Optional<UserEntity> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new DuplicateCredentialsException("Project Manager not found");
        }

        user.get().setEnabled(validity);
        user.get().setLastUpdatedAt(Instant.now());

        userRepository.save(user.get());

        return "Project Manager validity updated";
    }

    public String changeDetails(Long id, ChangeDetailsRequest request) {
        Optional<UserEntity> userOpt = userRepository.findById(id);

        if (userOpt.isEmpty()) {
            throw new DuplicateCredentialsException("User not found");
        }

        UserEntity user = userOpt.get();
        boolean isUpdated = false;

        if (request.name() != null && !request.name().isBlank()) {
            user.setUsername(request.name());
            isUpdated = true;
        }

        if (request.email() != null && !request.email().isBlank()) {
            user.setEmail(request.email());
            isUpdated = true;
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
            isUpdated = true;
        }

        if (isUpdated) {
            user.setLastUpdatedAt(Instant.now());
            userRepository.save(user);
            return "Details updated successfully";
        }

        return "No changes provided";
    }
}
