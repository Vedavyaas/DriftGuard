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
    public ResponseEntity<?> ingestEvents(@RequestBody List<Object> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No payloads provided"));
        }

        int sent = 0;
        for (Object payload : payloads) {
            if (payload instanceof String) {
                // It's a raw NLP text string. Send directly to Kafka!
                String text = (String) payload;
                if (!text.isBlank()) {
                    kafkaTemplate.send("drift-events", text);
                    sent++;
                }
            } else {
                // It's a JSON object representing a DriftEventDTO
                try {
                    DriftEventDTO event = objectMapper.convertValue(payload, DriftEventDTO.class);
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

                    String eventJson = objectMapper.writeValueAsString(event);
                    kafkaTemplate.send("drift-events", eventJson);
                    sent++;
                } catch (Exception e) {
                    System.err.println("Failed to serialize event: " + e.getMessage());
                }
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
            List<Object> payloads = objectMapper.readValue(
                    file.getInputStream(),
                    new TypeReference<List<Object>>() {}
            );
            // Stamp projectHash onto every event if provided and if it's a Map (JSON object)
            if (projectHash != null && !projectHash.isBlank()) {
                for (Object payload : payloads) {
                    if (payload instanceof Map) {
                        ((Map<String, Object>) payload).put("projectHash", projectHash);
                    }
                }
            }
            return ingestEvents(payloads);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse JSON file: " + e.getMessage()));
        }
    }
}
