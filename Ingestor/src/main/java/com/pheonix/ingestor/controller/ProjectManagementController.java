package com.pheonix.ingestor.controller;

import com.pheonix.ingestor.assets.DuplicateCredentialsException;
import com.pheonix.ingestor.assets.ProjectCreationCredentials;
import com.pheonix.ingestor.assets.ProjectDTO;
import com.pheonix.ingestor.assets.Status;
import com.pheonix.ingestor.service.ProjectManagementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class ProjectManagementController {
    private final ProjectManagementService projectManagementService;

    public ProjectManagementController(ProjectManagementService projectManagementService) {
        this.projectManagementService = projectManagementService;
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @PostMapping(value = "/enter/project/details", consumes = {"multipart/form-data"})
    public ResponseEntity<String> projectCreation(@RequestPart("projectCreationCredentials") ProjectCreationCredentials projectCreationCredentials, @RequestPart("baseLineFile") MultipartFile baseLineFile, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectManagementService.createProject(jwt.getSubject(), projectCreationCredentials, baseLineFile));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @PutMapping("/change/baseline")
    public ResponseEntity<String> changeBaseLineFile(@RequestParam Long id, @RequestPart MultipartFile baselineFile, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectManagementService.changeBaseLineFile(id, baselineFile, jwt.getSubject()));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @PutMapping("/change/status")
    public ResponseEntity<String> changeStatus(@RequestParam Long id, @RequestParam Status status, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectManagementService.changeStatus(id, status, jwt.getSubject()));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @PutMapping("/change/ignored-domains")
    public ResponseEntity<String> changeIgnoredDomains(@RequestParam String hash, @RequestParam String ignoredDomains, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectManagementService.changeIgnoredDomains(hash, ignoredDomains, jwt.getSubject()));
    }

    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @GetMapping("/get/project/info")
    public ResponseEntity<List<ProjectDTO>> getProjectInfo(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(projectManagementService.getInfo(jwt.getSubject()));
    }

    @ExceptionHandler(DuplicateCredentialsException.class)
    public ResponseEntity<String> handle(DuplicateCredentialsException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
