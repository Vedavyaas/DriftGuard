package com.pheonix.ingestor.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Stores the raw JSON of each incoming drift event, per project.
 * Used for Groq report generation (on-demand only).
 * Keeps only the latest MAX_PER_PROJECT events per project hash.
 */
@Entity
@Table(name = "raw_drift_events",
       indexes = { @Index(name = "idx_raw_event_project_hash", columnList = "project_hash"),
                   @Index(name = "idx_raw_event_received_at",  columnList = "received_at") })
public class RawDriftEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_hash", nullable = false, length = 128)
    private String projectHash;

    @Column(name = "event_json", nullable = false, columnDefinition = "TEXT")
    private String eventJson;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public RawDriftEvent() {}

    public RawDriftEvent(String projectHash, String eventJson) {
        this.projectHash = projectHash;
        this.eventJson = eventJson;
        this.receivedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getProjectHash() { return projectHash; }
    public String getEventJson() { return eventJson; }
    public LocalDateTime getReceivedAt() { return receivedAt; }
}
