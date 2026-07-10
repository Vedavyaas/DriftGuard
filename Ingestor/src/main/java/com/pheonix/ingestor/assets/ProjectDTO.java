package com.pheonix.ingestor.assets;

import java.time.Instant;

public record ProjectDTO(Long id, String projectName, Instant createdAt, Status status, String projectHash) {
}
