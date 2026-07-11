package com.pheonix.ingestor.service;

import com.pheonix.ingestor.assets.ProjectStatus;
import com.pheonix.ingestor.model.ThreatIncident;
import com.pheonix.ingestor.repository.ProjectManagerEntity;
import com.pheonix.ingestor.repository.ProjectManagerRepository;
import com.pheonix.ingestor.repository.ThreatIncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectHealthService {

    private final ProjectManagerRepository projectManagerRepo;
    private final ThreatIncidentRepository incidentRepo;

    public ProjectHealthService(ProjectManagerRepository projectManagerRepo, ThreatIncidentRepository incidentRepo) {
        this.projectManagerRepo = projectManagerRepo;
        this.incidentRepo = incidentRepo;
    }

    public void updateProjectHealth(String projectHash) {
        ProjectManagerEntity project = projectManagerRepo.findByProjectHash(projectHash).orElse(null);
        if (project == null) return;

        List<ThreatIncident> incidents = incidentRepo.findByProjectHashOrderByDetectedAtDesc(projectHash);
        
        int penalty = 0;
        for (ThreatIncident inc : incidents) {
            if (inc.getStatus() != ProjectStatus.RESOLVED) {
                switch (inc.getSeverity()) {
                    case CRITICAL: penalty += 20; break;
                    case HIGH: penalty += 10; break;
                    case MEDIUM: penalty += 5; break;
                    case LOW: penalty += 1; break;
                    default: break;
                }
            }
        }
        
        int health = 100 - penalty;
        if (health < 0) health = 0;
        if (health > 100) health = 100;
        
        project.setHealthScore(health);
        projectManagerRepo.save(project);
    }
}
