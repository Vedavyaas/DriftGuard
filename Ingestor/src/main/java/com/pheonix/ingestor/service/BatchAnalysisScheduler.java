package com.pheonix.ingestor.service;

import com.pheonix.ingestor.assets.ProjectSeverity;
import com.pheonix.ingestor.assets.ProjectStatus;
import com.pheonix.ingestor.model.ThreatIncident;
import com.pheonix.ingestor.repository.ProjectManagerEntity;
import com.pheonix.ingestor.repository.ProjectManagerRepository;
import com.pheonix.ingestor.repository.ThreatIncidentRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Sweeps for any UNREAD CRITICAL incidents every 2 minutes and sends
 * email alerts via the Kafka mail_listener topic → Auth.SimpleMailSender.
 *
 * (Batch correlation is now event-count driven inside DriftEventKafkaConsumer.)
 */
@Service
public class BatchAnalysisScheduler {

    private final ThreatIncidentRepository incidentRepo;
    private final ProjectManagerRepository projectRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public BatchAnalysisScheduler(ThreatIncidentRepository incidentRepo,
                                  ProjectManagerRepository projectRepo,
                                  KafkaTemplate<String, String> kafkaTemplate) {
        this.incidentRepo = incidentRepo;
        this.projectRepo = projectRepo;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 120_000, initialDelay = 60_000)
    public void alertUnreadCriticals() {
        List<ThreatIncident> unread = incidentRepo
                .findByStatusAndSeverity(ProjectStatus.UNREAD, ProjectSeverity.CRITICAL);

        if (unread.isEmpty()) return;

        System.out.println("[AlertSweep] " + unread.size() + " unread CRITICAL incident(s) — sending alerts.");

        for (ThreatIncident incident : unread) {
            ProjectManagerEntity project = projectRepo
                    .findByProjectHash(incident.getProjectHash()).orElse(null);
            if (project == null) continue;

            String managerName = project.getManagerName();
            String shortDesc = incident.getProblemDescription() != null
                    ? incident.getProblemDescription()
                            .substring(0, Math.min(100, incident.getProblemDescription().length()))
                            .replaceAll("\\s+", "_")
                    : "incident";

            // Format: "managerName <space> message" — matches SimpleMailSender
            kafkaTemplate.send("mail_listener", managerName + " UNREAD_CRITICAL:" + shortDesc);

            incident.setStatus(ProjectStatus.NOTIFIED);
            incidentRepo.save(incident);

            System.out.println("[AlertSweep] Notified manager " + managerName
                    + " for incident " + incident.getIncidentId());
        }
    }
}
