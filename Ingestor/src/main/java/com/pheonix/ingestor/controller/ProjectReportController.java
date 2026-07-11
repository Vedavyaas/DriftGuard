package com.pheonix.ingestor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pheonix.ingestor.assets.ProjectSeverity;
import com.pheonix.ingestor.assets.ProjectStatus;
import com.pheonix.ingestor.dto.DriftEventDTO;
import com.pheonix.ingestor.dto.PythonBatchRequest;
import com.pheonix.ingestor.model.RawDriftEvent;
import com.pheonix.ingestor.model.ThreatIncident;
import com.pheonix.ingestor.repository.RawDriftEventRepository;
import com.pheonix.ingestor.repository.ThreatIncidentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ProjectReportController {

    private final ThreatIncidentRepository incidentRepo;
    private final RawDriftEventRepository rawEventRepo;
    private final com.pheonix.ingestor.service.ProjectHealthService projectHealthService;
    private final com.pheonix.ingestor.repository.ProjectManagerRepository projectManagerRepo;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String PYTHON_URL = "http://localhost:9003";

    public ProjectReportController(ThreatIncidentRepository incidentRepo,
                                   RawDriftEventRepository rawEventRepo,
                                   com.pheonix.ingestor.service.ProjectHealthService projectHealthService,
                                   com.pheonix.ingestor.repository.ProjectManagerRepository projectManagerRepo) {
        this.incidentRepo = incidentRepo;
        this.rawEventRepo = rawEventRepo;
        this.projectHealthService = projectHealthService;
        this.projectManagerRepo = projectManagerRepo;
    }

    /** All incidents for a project, newest first. */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @GetMapping("/project/{projectHash}")
    public ResponseEntity<List<ThreatIncident>> getProjectIncidents(
            @PathVariable String projectHash) {
        return ResponseEntity.ok(incidentRepo.findByProjectHashOrderByDetectedAtDesc(projectHash));
    }

    /** Global summary counts. */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        List<ThreatIncident> all = incidentRepo.findAll();
        return ResponseEntity.ok(Map.of(
            "total_incidents",  all.size(),
            "critical",  all.stream().filter(i -> i.getSeverity() == ProjectSeverity.CRITICAL).count(),
            "high",      all.stream().filter(i -> i.getSeverity() == ProjectSeverity.HIGH).count(),
            "unread",    all.stream().filter(i -> i.getStatus() == ProjectStatus.UNREAD).count(),
            "notified",  all.stream().filter(i -> i.getStatus() == ProjectStatus.NOTIFIED).count(),
            "resolved",  all.stream().filter(i -> i.getStatus() == ProjectStatus.RESOLVED).count()
        ));
    }

    /** Admin manager stats aggregation. */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_ADMIN')")
    @GetMapping("/admin/manager-stats")
    public ResponseEntity<Map<String, Object>> getAdminManagerStats() {
        List<com.pheonix.ingestor.repository.ProjectManagerEntity> allProjects = projectManagerRepo.findAll();
        List<ThreatIncident> allIncidents = incidentRepo.findAll();

        Map<String, com.pheonix.ingestor.dto.ManagerStatsDTO> statsMap = new java.util.HashMap<>();
        int totalHealth = 0;
        int validProjects = 0;

        List<Map<String, Object>> projectsHealth = new java.util.ArrayList<>();

        for (com.pheonix.ingestor.repository.ProjectManagerEntity proj : allProjects) {
            String manager = proj.getManagerName();
            statsMap.putIfAbsent(manager, new com.pheonix.ingestor.dto.ManagerStatsDTO(manager, 0, 0.0, 0));
            
            Integer health = proj.getHealthScore();
            if (health == null) {
                // Retroactively fix legacy projects
                projectHealthService.updateProjectHealth(proj.getProjectHash());
                // Fetch the updated project
                com.pheonix.ingestor.repository.ProjectManagerEntity updated = projectManagerRepo.findById(proj.getId()).orElse(proj);
                health = updated.getHealthScore();
                if (health == null) health = 100;
            }

            com.pheonix.ingestor.dto.ManagerStatsDTO stat = statsMap.get(manager);
            stat.setProjectCount(stat.getProjectCount() + 1);
            stat.setAverageHealth(stat.getAverageHealth() + health);

            totalHealth += health;
            validProjects++;
            
            projectsHealth.add(Map.of(
                "projectName", proj.getProjectName(),
                "managerName", manager,
                "healthScore", health
            ));
        }

        // Add incidents count per manager based on their projects
        for (ThreatIncident inc : allIncidents) {
            if (inc.getStatus() != ProjectStatus.RESOLVED) {
                com.pheonix.ingestor.repository.ProjectManagerEntity proj = allProjects.stream()
                        .filter(p -> p.getProjectHash().equals(inc.getProjectHash()))
                        .findFirst().orElse(null);
                
                if (proj != null) {
                    com.pheonix.ingestor.dto.ManagerStatsDTO stat = statsMap.get(proj.getManagerName());
                    stat.setActiveIncidents(stat.getActiveIncidents() + 1);
                }
            }
        }

        // Finalize averages
        for (com.pheonix.ingestor.dto.ManagerStatsDTO stat : statsMap.values()) {
            if (stat.getProjectCount() > 0) {
                stat.setAverageHealth(stat.getAverageHealth() / stat.getProjectCount());
            }
        }

        double overallHealth = validProjects > 0 ? (double) totalHealth / validProjects : 100.0;

        return ResponseEntity.ok(Map.of(
            "overall_system_health", Math.round(overallHealth),
            "manager_stats", statsMap.values(),
            "projects_health", projectsHealth
        ));
    }

    /** Update incident status (UNREAD → RESOLVED, etc.). */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @PutMapping("/incident/{id}/status")
    public ResponseEntity<String> updateStatus(
            @PathVariable String id,
            @RequestParam ProjectStatus status) {
        return incidentRepo.findById(id).map(incident -> {
            incident.setStatus(status);
            incidentRepo.save(incident);
            projectHealthService.updateProjectHealth(incident.getProjectHash());
            return ResponseEntity.ok("Status updated to " + status);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Generate a Groq LLM report for a project.
     * Calls Python /report/generate with recent events from the buffer store.
     * Only triggered when the user explicitly clicks "Generate Report".
     */
    @PreAuthorize("hasAuthority('SCOPE_ROLE_PROJECTMANAGER')")
    @GetMapping("/project/{projectHash}/generate")
    public ResponseEntity<?> generateReport(@PathVariable String projectHash) {
        List<RawDriftEvent> rawEvents = rawEventRepo.findTop100ByProjectHashOrderByReceivedAtDesc(projectHash);
        List<DriftEventDTO> recentEvents = new java.util.ArrayList<>();
        for (RawDriftEvent raw : rawEvents) {
            try {
                recentEvents.add(objectMapper.readValue(raw.getEventJson(), DriftEventDTO.class));
            } catch (Exception ignored) {}
        }

        if (recentEvents.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "report_title", "DriftGuard Report",
                "executive_narrative", "No events have been received for this project yet. " +
                    "Send events via Kafka first, then generate the report.",
                "summary", Map.of("total_events_analyzed", 0),
                "critical_incidents", List.of()
            ));
        }

        try {
            PythonBatchRequest request = new PythonBatchRequest(recentEvents);
            Object report = restTemplate.postForObject(PYTHON_URL + "/report/generate", request, Object.class);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.status(503).body(
                Map.of("error", "Python ML service unavailable: " + e.getMessage())
            );
        }
    }
}
