package com.pheonix.ingestor.model;

import com.pheonix.ingestor.assets.ProjectSeverity;
import com.pheonix.ingestor.assets.ProjectStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threat_incidents")
public class ThreatIncident {

    @Id
    private String incidentId;

    private String projectHash;

    @Enumerated(EnumType.STRING)
    private ProjectSeverity severity;

    @Enumerated(EnumType.STRING)
    private ProjectStatus status;

    private LocalDateTime detectedAt;

    @Column(columnDefinition = "TEXT")
    private String problemDescription;

    @Column(columnDefinition = "TEXT")
    private String remediationSteps;

    @Column(columnDefinition = "TEXT")
    private String graphData;

    private String domain;

    public ThreatIncident() {
    }

    public ThreatIncident(String incidentId, String projectHash, ProjectSeverity severity, ProjectStatus status, String problemDescription, String remediationSteps, String graphData, String domain) {
        this.incidentId = incidentId;
        this.projectHash = projectHash;
        this.severity = severity;
        this.status = status;
        this.detectedAt = LocalDateTime.now();
        this.problemDescription = problemDescription;
        this.remediationSteps = remediationSteps;
        this.graphData = graphData;
        this.domain = domain;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public String getProjectHash() {
        return projectHash;
    }

    public void setProjectHash(String projectHash) {
        this.projectHash = projectHash;
    }

    public ProjectSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(ProjectSeverity severity) {
        this.severity = severity;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getRemediationSteps() {
        return remediationSteps;
    }

    public void setRemediationSteps(String remediationSteps) {
        this.remediationSteps = remediationSteps;
    }

    public String getGraphData() {
        return graphData;
    }

    public void setGraphData(String graphData) {
        this.graphData = graphData;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
