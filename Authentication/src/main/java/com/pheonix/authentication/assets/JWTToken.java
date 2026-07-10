package com.pheonix.authentication.assets;

public record JWTToken(String token, String username, Role role) {
}
