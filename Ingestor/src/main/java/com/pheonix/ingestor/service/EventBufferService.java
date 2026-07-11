package com.pheonix.ingestor.service;

import com.pheonix.ingestor.dto.DriftEventDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * In-memory sliding window buffer (15-minute window) per project.
 * Used solely for batch ML correlation (5+ events trigger).
 *
 * Raw event persistence for report generation is handled by
 * RawDriftEventRepository → PostgreSQL.
 */
@Service
public class EventBufferService {

    private final Map<String, Deque<DriftEventDTO>> buffer = new ConcurrentHashMap<>();
    private static final int WINDOW_MINUTES = 15;

    public void addEvent(String projectHash, DriftEventDTO event) {
        buffer.computeIfAbsent(projectHash, k -> new ConcurrentLinkedDeque<>()).addLast(event);
    }

    public int getCount(String projectHash) {
        Deque<DriftEventDTO> q = buffer.get(projectHash);
        return q == null ? 0 : q.size();
    }

    public List<DriftEventDTO> getEventsForProject(String projectHash) {
        Deque<DriftEventDTO> q = buffer.get(projectHash);
        return (q == null || q.isEmpty()) ? new ArrayList<>() : new ArrayList<>(q);
    }

    public void clearBuffer(String projectHash) {
        Deque<DriftEventDTO> q = buffer.get(projectHash);
        if (q != null) q.clear();
    }

    public List<String> getActiveProjects() {
        return new ArrayList<>(buffer.keySet());
    }

    @Scheduled(fixedRate = 60_000)
    public void evictOldEvents() {
        Instant cutoff = Instant.now().minus(WINDOW_MINUTES, ChronoUnit.MINUTES);
        for (Map.Entry<String, Deque<DriftEventDTO>> entry : buffer.entrySet()) {
            Deque<DriftEventDTO> queue = entry.getValue();
            while (!queue.isEmpty()) {
                DriftEventDTO oldest = queue.peekFirst();
                if (oldest != null && oldest.getTimestamp() != null) {
                    try {
                        if (Instant.parse(oldest.getTimestamp()).isBefore(cutoff)) {
                            queue.pollFirst();
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        queue.pollFirst();
                    }
                } else {
                    queue.pollFirst();
                }
            }
        }
    }
}
