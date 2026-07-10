package com.pheonix.authentication.controller;

import com.pheonix.authentication.assets.*;
import com.pheonix.authentication.service.UserCreationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserCreationController {
    private final UserCreationService userCreationService;

    public UserCreationController(UserCreationService userCreationService) {
        this.userCreationService = userCreationService;
    }

    @PostMapping("/api/user/login")
    public ResponseEntity<JWTToken> login(@RequestBody LoginCredentials loginCredentials) {
        return ResponseEntity.ok(userCreationService.authenticate(loginCredentials));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @PostMapping("/create/managers")
    public ResponseEntity<String> createProjectManagers(@RequestBody ProjectManagerCredentials credentials) {
        return ResponseEntity.ok(userCreationService.createProjectManager(credentials));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @GetMapping("/get/manager/info")
    public ResponseEntity<List<ProjectManagerDTO>> getProjectManagersInfo() {
        return ResponseEntity.ok(userCreationService.getProjectManagers());
    }

    @GetMapping("/get/self/info")
    public ResponseEntity<ProjectManagerDTO> getSelfInfo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userCreationService.getSelfInfo(jwt.getSubject()));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @PutMapping("/change/validity/manager")
    public ResponseEntity<String> changeStateOfProjectManager(@RequestParam Long id, @RequestParam boolean validity) {
        return ResponseEntity.ok(userCreationService.changeProjectManagerValidity(id, validity));
    }

    @PutMapping("/change/details")
    public ResponseEntity<String> changeCredentials(@RequestParam Long id, @RequestBody ChangeDetailsRequest request) {
        return ResponseEntity.ok(userCreationService.changeDetails(id, request));
    }

    @ExceptionHandler(DuplicateCredentialsException.class)
    public ResponseEntity<String> handle(DuplicateCredentialsException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
