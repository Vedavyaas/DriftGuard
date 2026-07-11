package com.pheonix.ingestor.repository;

import com.pheonix.ingestor.assets.ProjectSeverity;
import com.pheonix.ingestor.assets.ProjectStatus;
import com.pheonix.ingestor.model.ThreatIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThreatIncidentRepository extends JpaRepository<ThreatIncident, String> {
    List<ThreatIncident> findByProjectHash(String projectHash);
    List<ThreatIncident> findByStatusAndSeverity(ProjectStatus status, ProjectSeverity severity);
    List<ThreatIncident> findByStatus(ProjectStatus status);
    List<ThreatIncident> findByProjectHashOrderByDetectedAtDesc(String projectHash);
}
