package com.pheonix.ingestor.service;

import com.pheonix.ingestor.assets.DuplicateCredentialsException;
import com.pheonix.ingestor.assets.HashUtil;
import com.pheonix.ingestor.assets.ProjectCreationCredentials;
import com.pheonix.ingestor.assets.ProjectDTO;
import com.pheonix.ingestor.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ProjectManagementService {
    private final ProjectManagerRepository projectManagerRepository;


    public ProjectManagementService(ProjectManagerRepository projectManagerRepository) {
        this.projectManagerRepository = projectManagerRepository;
    }


    @Transactional
    public String createProject(String managerName, ProjectCreationCredentials projectCreationCredentials, MultipartFile baseLineFile) {
        if (projectManagerRepository.existsByProjectName(projectCreationCredentials.projectName())) {
            throw new DuplicateCredentialsException("Project name already exist");
        }
        byte[] baselineFileBytes = null;
        if (baseLineFile != null && !baseLineFile.isEmpty()) {
            try {
                baselineFileBytes = baseLineFile.getBytes();
            } catch (IOException e) {
                throw new RuntimeException("Failed to read baseline file", e);
            }
        }

        ProjectManagerEntity projectManagerEntity = new ProjectManagerEntity(managerName, projectCreationCredentials.projectName(), baselineFileBytes);
        projectManagerEntity.setStatus(projectCreationCredentials.status());
        projectManagerRepository.save(projectManagerEntity);

        Long id = projectManagerEntity.getId();
        String projectName = projectManagerEntity.getProjectName();
        String managerEntityName = projectManagerEntity.getManagerName();

        String hash = HashUtil.sha256(id + projectName + managerEntityName);
        projectManagerEntity.setProjectHash(hash);
        projectManagerRepository.save(projectManagerEntity);

        return "Created Successfully";
    }

    public List<ProjectDTO> getInfo(String managerName) {
        return projectManagerRepository.findAllByManagerName(managerName);
    }

    @Transactional
    public String changeStatus(Long id, com.pheonix.ingestor.assets.Status status, String managerName) {
        ProjectManagerEntity projectManagerEntity = projectManagerRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        if (!projectManagerEntity.getManagerName().equals(managerName)) {
            throw new RuntimeException("Unauthorized");
        }
        projectManagerEntity.setStatus(status);
        projectManagerRepository.save(projectManagerEntity);
        return "Status changed successfully";
    }

    @Transactional
    public String changeIgnoredDomains(String projectHash, String ignoredDomains, String managerName) {
        ProjectManagerEntity projectManagerEntity = projectManagerRepository.findByProjectHash(projectHash)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        if (!projectManagerEntity.getManagerName().equals(managerName)) {
            throw new RuntimeException("Unauthorized");
        }
        projectManagerEntity.setIgnoredDomains(ignoredDomains);
        projectManagerRepository.save(projectManagerEntity);
        return "Ignored domains updated successfully";
    }

    @Transactional
    public String changeBaseLineFile(Long id, MultipartFile baseLineFile, String managerName) {
        ProjectManagerEntity projectManagerEntity = projectManagerRepository.findById(id).orElseThrow(() -> new RuntimeException("Project not found"));
        if (!projectManagerEntity.getManagerName().equals(managerName)) {
            throw new RuntimeException("Unauthorized");
        }

        if (baseLineFile != null && !baseLineFile.isEmpty()) {
            try {
                projectManagerEntity.setBaselineFile(baseLineFile.getBytes());
                projectManagerRepository.save(projectManagerEntity);
                return "Baseline file updated successfully";
            } catch (IOException e) {
                throw new RuntimeException("Failed to store baseline file", e);
            }
        }
        throw new RuntimeException("Baseline file is empty");
    }
}