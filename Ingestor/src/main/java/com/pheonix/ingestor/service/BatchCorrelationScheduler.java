package com.pheonix.ingestor.service;

import com.pheonix.ingestor.assets.ProjectSeverity;
import com.pheonix.ingestor.assets.ProjectStatus;
import com.pheonix.ingestor.dto.CompoundIncidentDTO;
import com.pheonix.ingestor.dto.DriftEventDTO;
import com.pheonix.ingestor.dto.PythonResponse;
import com.pheonix.ingestor.model.ThreatIncident;
import com.pheonix.ingestor.repository.ThreatIncidentRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class BatchCorrelationScheduler {

    private final EventBufferService bufferService;
    private final MLIntelligenceClient mlClient;
    private final ThreatIncidentRepository incidentRepo;

    public BatchCorrelationScheduler(EventBufferService bufferService,
                                     MLIntelligenceClient mlClient,
                                     ThreatIncidentRepository incidentRepo) {
        this.bufferService = bufferService;
        this.mlClient = mlClient;
        this.incidentRepo = incidentRepo;
    }

    // Run every 5 minutes to correlate events
    @Scheduled(fixedRate = 10_000)
    public void runBatchCorrelation() {
        List<String> activeProjects = bufferService.getActiveProjects();
        System.out.println("Running batch correlation for " + activeProjects.size() + " active projects.");

        for (String projectHash : activeProjects) {
            List<DriftEventDTO> events = bufferService.getEventsForProject(projectHash);
            if (events == null || events.isEmpty()) {
                continue;
            }

            PythonResponse response = mlClient.analyzeBatch(events);
            
            if (response != null && response.getCompoundIncidents() != null) {
                for (CompoundIncidentDTO compound : response.getCompoundIncidents()) {
                    String severityStr = compound.getMaxSeverity();
                    if ("CRITICAL".equalsIgnoreCase(severityStr) || "HIGH".equalsIgnoreCase(severityStr)) {
                        saveCompoundThreatIncident(projectHash, compound);
                        System.out.println("ALERT: High/Critical compound incident detected and persisted!");
                        // TODO: Trigger Email Notification
                    }
                }
            }
        }
    }

    private void saveCompoundThreatIncident(String projectHash, CompoundIncidentDTO compound) {
        ProjectSeverity severity = ProjectSeverity.MEDIUM;
        try {
            severity = ProjectSeverity.valueOf(compound.getMaxSeverity().toUpperCase());
        } catch (Exception ignored) {}

        String domainsStr = compound.getDomains() != null ? String.join(", ", compound.getDomains()) : "Unknown";
        String description = "Compound Incident detected involving domains: " + domainsStr;
        
        String remediation = "Compliance violations: " + 
                (compound.getComplianceViolations() != null ? String.join(", ", compound.getComplianceViolations()) : "None");

        ThreatIncident incident = new ThreatIncident(
                compound.getIncidentId() != null ? compound.getIncidentId() : UUID.randomUUID().toString(),
                projectHash,
                severity,
                ProjectStatus.UNREAD,
                description,
                remediation,
                "{}", // Assuming full graph logic or AI response goes here later
                domainsStr
        );
        incidentRepo.save(incident);
    }
}
