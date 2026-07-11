package com.pheonix.ingestor.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pheonix.ingestor.assets.HashUtil;
import com.pheonix.ingestor.assets.Status;
import com.pheonix.ingestor.dto.DriftEventDTO;
import com.pheonix.ingestor.repository.ProjectManagerEntity;
import com.pheonix.ingestor.repository.ProjectManagerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventIngestionController {

    private final ProjectManagerRepository projectRepo;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public EventIngestionController(ProjectManagerRepository projectRepo, KafkaTemplate<String, String> kafkaTemplate) {
        this.projectRepo = projectRepo;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/ingest")
    public ResponseEntity<?> ingestEvents(@RequestBody List<DriftEventDTO> events) {
        if (events == null || events.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No events provided"));
        }

        int sent = 0;
        for (DriftEventDTO event : events) {
            String hash = event.getProjectHash();
            
            // If there's no project hash, or project doesn't exist, make it!
            if (hash == null || hash.isBlank() || projectRepo.findByProjectHash(hash).isEmpty()) {
                ProjectManagerEntity newProject = new ProjectManagerEntity(
                        "Auto-Generated Manager", 
                        "Auto-Project-" + UUID.randomUUID().toString().substring(0, 5), 
                        "{}".getBytes()
                );
                newProject.setStatus(Status.NOT_STARTED);
                projectRepo.save(newProject);

                // Generate hash if it was null/blank
                if (hash == null || hash.isBlank()) {
                    hash = HashUtil.sha256(newProject.getId() + newProject.getProjectName() + newProject.getManagerName());
                }
                
                newProject.setProjectHash(hash);
                projectRepo.save(newProject);
                
                // Assign the newly ensured hash back to the event
                event.setProjectHash(hash);
                System.out.println("Created new project for hash: " + hash);
            }

            try {
                String eventJson = objectMapper.writeValueAsString(event);
                kafkaTemplate.send("drift-events", eventJson);
                sent++;
            } catch (JsonProcessingException e) {
                System.err.println("Failed to serialize event: " + e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of("status", "success", "events_sent", sent));
    }

    /**
     * POST /api/events/ingest/file
     * Accepts a JSON file (multipart/form-data) containing an array of DriftEventDTO.
     * Parses the file and publishes each event to the Kafka drift-events topic.
     */
    @PostMapping("/ingest/file")
    public ResponseEntity<?> ingestFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "projectHash", required = false) String projectHash) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file provided"));
        }
        try {
            List<DriftEventDTO> events = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<DriftEventDTO>>() {}
            );
            // Stamp projectHash onto every event if provided
            if (projectHash != null && !projectHash.isBlank()) {
                events.forEach(e -> e.setProjectHash(projectHash));
            }
            // Reuse the same publish logic
            return ingestEvents(events);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse JSON file: " + e.getMessage()));
        }
    }
}
