package com.pheonix.ingestor.controller;

import com.pheonix.ingestor.model.ThreatIncident;
import com.pheonix.ingestor.repository.ThreatIncidentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class ThreatIncidentController {

    private final ThreatIncidentRepository incidentRepo;
    private final com.pheonix.ingestor.service.ProjectHealthService projectHealthService;

    public ThreatIncidentController(ThreatIncidentRepository incidentRepo,
                                    com.pheonix.ingestor.service.ProjectHealthService projectHealthService) {
        this.incidentRepo = incidentRepo;
        this.projectHealthService = projectHealthService;
    }

    @GetMapping
    public ResponseEntity<List<ThreatIncident>> getAllIncidents() {
        return ResponseEntity.ok(incidentRepo.findAll());
    }

    @GetMapping("/project/{projectHash}")
    public ResponseEntity<List<ThreatIncident>> getIncidentsByProject(@PathVariable String projectHash) {
        return ResponseEntity.ok(incidentRepo.findByProjectHash(projectHash));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThreatIncident> getIncidentById(@PathVariable String id) {
        return incidentRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** DELETE /api/incidents/clear — wipe ALL incidents (for testing). */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearAllIncidents() {
        long count = incidentRepo.count();
        incidentRepo.deleteAll();
        return ResponseEntity.ok("Cleared " + count + " incident(s).");
    }

    /** DELETE /api/incidents/project/{projectHash}/clear — wipe incidents for one project. */
    @DeleteMapping("/project/{projectHash}/clear")
    public ResponseEntity<String> clearProjectIncidents(@PathVariable String projectHash) {
        List<ThreatIncident> incidents = incidentRepo.findByProjectHash(projectHash);
        incidentRepo.deleteAll(incidents);
        projectHealthService.updateProjectHealth(projectHash);
        return ResponseEntity.ok("Cleared " + incidents.size() + " incident(s) for project " + projectHash);
    }
}
