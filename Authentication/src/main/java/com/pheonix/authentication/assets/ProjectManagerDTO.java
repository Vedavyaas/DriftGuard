package com.pheonix.authentication.assets;

import java.time.Instant;

public record ProjectManagerDTO(Long id, String username, String email, Role role, boolean isEnabled, Instant createdAt, Instant lastUpdatedAt) {
}
