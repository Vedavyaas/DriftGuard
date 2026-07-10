package com.pheonix.ingestor.repository;

import com.pheonix.ingestor.assets.Status;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class ProjectManagerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String managerName;
    private String projectName;
    private Instant createdAt;
    private Status status;
    private String projectHash;

    @Column(columnDefinition="bytea")
    private byte[] baselineFile;

    public ProjectManagerEntity() {
    }

    public ProjectManagerEntity(String managerName, String projectName, byte[] baselineFile) {
        this.managerName = managerName;
        this.projectName = projectName;
        this.createdAt = Instant.now();
        this.baselineFile = baselineFile;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public byte[] getBaselineFile() {
        return baselineFile;
    }

    public void setBaselineFile(byte[] baselineFile) {
        this.baselineFile = baselineFile;
    }

    public String getProjectHash() {
        return projectHash;
    }

    public void setProjectHash(String projectHash) {
        this.projectHash = projectHash;
    }
}
