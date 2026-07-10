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
    @PostMapping("/create/analysts")
    public ResponseEntity<String> createAnalysts(@RequestBody AnalystCredentials analystCredentials) {
        return ResponseEntity.ok(userCreationService.createAnalyst(analystCredentials));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @GetMapping("/get/ananlyst/info")
    public ResponseEntity<List<AnalystDTO>> getAnalystsInfo() {
        return ResponseEntity.ok(userCreationService.getAnalysts());
    }

    @GetMapping("/get/self/info")
    public ResponseEntity<AnalystDTO> getSelfInfo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userCreationService.getSelfInfo(jwt.getSubject()));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @PutMapping("/change/validity/analyst")
    public ResponseEntity<String> changeStateOfAnalyst(@RequestParam Long id, @RequestParam boolean validity) {
        return ResponseEntity.ok(userCreationService.changeAnalystValidity(id, validity));
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
