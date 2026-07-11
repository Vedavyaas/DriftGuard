package com.pheonix.ingestor.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pheonix.ingestor.assets.ProjectSeverity;
import com.pheonix.ingestor.assets.ProjectStatus;
import com.pheonix.ingestor.dto.CompoundIncidentDTO;
import com.pheonix.ingestor.dto.DriftEventDTO;
import com.pheonix.ingestor.dto.PythonResponse;
import com.pheonix.ingestor.model.RawDriftEvent;
import com.pheonix.ingestor.model.ThreatIncident;
import com.pheonix.ingestor.repository.ProjectManagerEntity;
import com.pheonix.ingestor.repository.ProjectManagerRepository;
import com.pheonix.ingestor.repository.RawDriftEventRepository;
import com.pheonix.ingestor.repository.ThreatIncidentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DriftEventKafkaConsumer {

    private static final int BATCH_THRESHOLD = 5;

    private final ProjectManagerRepository projectRepo;
    private final ThreatIncidentRepository incidentRepo;
    private final RawDriftEventRepository rawEventRepo;
    private final EventBufferService bufferService;
    private final MLIntelligenceClient mlClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final ProjectHealthService projectHealthService;

    public DriftEventKafkaConsumer(ProjectManagerRepository projectRepo,
                                   ThreatIncidentRepository incidentRepo,
                                   RawDriftEventRepository rawEventRepo,
                                   EventBufferService bufferService,
                                   MLIntelligenceClient mlClient,
                                   KafkaTemplate<String, String> kafkaTemplate,
                                   ProjectHealthService projectHealthService) {
        this.projectRepo = projectRepo;
        this.incidentRepo = incidentRepo;
        this.rawEventRepo = rawEventRepo;
        this.bufferService = bufferService;
        this.mlClient = mlClient;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
        this.projectHealthService = projectHealthService;
    }

    @Transactional
    @KafkaListener(topics = "drift-events", groupId = "projectGroup")
    public void consumeEvent(String message) {
        try {
            DriftEventDTO event = objectMapper.readValue(message, DriftEventDTO.class);
            String projectHash = event.getProjectHash();

            // ── 1. Resolve project ──────────────────────────────────────────────
            if (projectHash == null) {
                System.out.println("[Consumer] Dropped event: no project hash.");
                return;
            }
            ProjectManagerEntity project = projectRepo.findByProjectHash(projectHash).orElse(null);
            if (project == null) {
                System.out.println("[Consumer] Dropped event: project not found for hash " + projectHash);
                return;
            }
            String managerName = project.getManagerName();

            // ── 2. Persist raw event to PostgreSQL (for report generation) ──────
            rawEventRepo.save(new RawDriftEvent(projectHash, message));
            // Keep only latest 100 per project
            if (rawEventRepo.countByProjectHash(projectHash) > 100) {
                rawEventRepo.deleteOldestBeyondLimit(projectHash, 100);
            }

            // ── 3. Individual ML analysis ──────────────────────────────────────────
            PythonResponse single = mlClient.analyzeSingleEvent(event);
            if (single != null && Boolean.TRUE.equals(single.getIsRisky())) {
                String severity = single.getSeverity();
                String incidentId = saveIncidentFromSingleResponse(projectHash, event, single);
                // Immediately email manager for CRITICAL events
                if ("CRITICAL".equalsIgnoreCase(severity)) {
                    sendEmailAlert(managerName, incidentId, severity,
                            event.getSystem(), event.getDomain());
                }
            }

            // ── 3. Add to sliding window buffer (all events) ──────────────────────
            bufferService.addEvent(projectHash, event);

            // ── 4. Trigger batch analysis when threshold is reached ────────────────
            if (bufferService.getCount(projectHash) >= BATCH_THRESHOLD) {
                List<DriftEventDTO> batchEvents = bufferService.getEventsForProject(projectHash);
                bufferService.clearBuffer(projectHash); // Clear before async processing

                PythonResponse batchResponse = mlClient.analyzeBatch(batchEvents);
                if (batchResponse != null && batchResponse.getCompoundIncidents() != null) {
                    for (CompoundIncidentDTO compound : batchResponse.getCompoundIncidents()) {
                        String compSeverity = compound.getMaxSeverity();
                        if (compSeverity == null) continue;
                        String incidentId = saveCompoundIncident(projectHash, compound, batchEvents.size());
                        // Email for compound HIGH/CRITICAL
                        if ("CRITICAL".equalsIgnoreCase(compSeverity) || "HIGH".equalsIgnoreCase(compSeverity)) {
                            sendEmailAlert(managerName, incidentId, "COMPOUND-" + compSeverity,
                                    String.join(",", compound.getDomains() != null ? compound.getDomains() : List.of()),
                                    "multi-domain");
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[Consumer] Error processing Kafka message: " + e.getMessage());
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private String saveIncidentFromSingleResponse(String projectHash, DriftEventDTO event, PythonResponse r) {
        String mitre = nvl(r.getMitreTechnique(), "Unknown") + " – " + nvl(r.getMitreName(), "Unknown Technique");
        String actor = nvl(r.getActor(), nvl(event.getChangedBy(), "unknown"));
        String direction = nvl(r.getDeltaDirection(), "DEGRADED");
        double riskScore = r.getRiskScore() != null ? r.getRiskScore() : 0.0;

        String description = String.format(
            "[%s] Drift on '%s' (%s env).\nParameter '%s': '%s' → '%s' by '%s'.\nRisk: %.1f | %s\nMITRE: %s",
            r.getSeverity(), nvl(event.getSystem(), "?"), nvl(event.getEnvironment(), "?"),
            nvl(event.getParameter(), "?"), event.getOldValue(), event.getNewValue(),
            actor, riskScore, direction, mitre
        );
        String remediation = String.format(
            "1. Review change to '%s' on '%s'.\n2. Revert '%s' to safe baseline: '%s'.\n" +
            "3. Investigate actor '%s'.\n4. Validate domain '%s' baselines.\n5. Apply MITRE mitigations for %s.",
            nvl(event.getSystem(), "?"), nvl(event.getDomain(), "?"),
            nvl(event.getParameter(), "?"), event.getOldValue(),
            actor, nvl(event.getDomain(), "?"), mitre
        );

        return saveIncident(projectHash, r.getSeverity(), description, remediation, "{}");
    }

    private String saveCompoundIncident(String projectHash, CompoundIncidentDTO c, int eventCount) {
        String domains = c.getDomains() != null ? String.join(", ", c.getDomains()) : "unknown";
        String violations = c.getComplianceViolations() != null ? String.join(", ", c.getComplianceViolations()) : "none";
        String description = String.format(
            "[COMPOUND-%s] %d buffered events correlated across domains: %s.\nPython ID: %s\nCompliance: %s",
            c.getMaxSeverity(), eventCount, domains, nvl(c.getIncidentId(), "N/A"), violations
        );
        String remediation = String.format(
            "1. Review all drifts in domains: %s.\n2. Check if drifts are coordinated.\n" +
            "3. Compliance violations: %s.\n4. Escalate to security team.\n5. Revalidate baselines.",
            domains, violations
        );
        String graphDataStr = "{}";
        try {
            if (c.getGraphData() != null) {
                graphDataStr = objectMapper.writeValueAsString(c.getGraphData());
            }
        } catch (Exception e) {
            System.err.println("[Consumer] Failed to serialize graph data: " + e.getMessage());
        }

        return saveIncident(projectHash, c.getMaxSeverity(), description, remediation, graphDataStr);
    }

    private String saveIncident(String projectHash, String severityStr, String description, String remediation, String graphData) {
        ProjectSeverity severity = ProjectSeverity.MEDIUM;
        try { severity = ProjectSeverity.valueOf(severityStr.toUpperCase()); } catch (Exception ignored) {}
        String id = UUID.randomUUID().toString();
        incidentRepo.save(new ThreatIncident(id, projectHash, severity, ProjectStatus.UNREAD, description, remediation, graphData));
        projectHealthService.updateProjectHealth(projectHash);
        return id;
    }

    private void sendEmailAlert(String managerName, String incidentId, String severity, String system, String domain) {
        // Format matches Auth's SimpleMailSender: "managerName <space> message"
        String payload = managerName + " CRITICAL_DRIFT[" + severity + "] on " + system
                + " domain=" + domain + " incidentId=" + incidentId;
        kafkaTemplate.send("mail_listener", payload);
        System.out.println("[Consumer] Email alert sent for incident: " + incidentId);

        incidentRepo.findById(incidentId).ifPresent(incident -> {
            incident.setStatus(ProjectStatus.NOTIFIED);
            incidentRepo.save(incident);
        });
    }

    private String nvl(String value, String fallback) {
        return value != null ? value : fallback;
    }
}
