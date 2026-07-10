package com.pheonix.authentication.config;

import com.pheonix.authentication.assets.Role;
import com.pheonix.authentication.repository.UserEntity;
import com.pheonix.authentication.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByRole(Role.ADMIN)) {
            UserEntity user = new UserEntity("CGAdmin", "cgadmin@gmail.com",
                    passwordEncoder.encode("123"), Role.ADMIN, Instant.now());
            userRepository.save(user);

            logger.info("Database seeded with admin account");
        }

        if (!userRepository.existsByRole(Role.PROJECTMANAGER)) {
            UserEntity user = new UserEntity("CGManager", "cgmanager@gmail.com",
                    passwordEncoder.encode("123"), Role.PROJECTMANAGER, Instant.now());
            userRepository.save(user);
            logger.info("Database seeded with manager account");
        }
    }
}
