package com.homeprotectors.backend.dto.auth;

public record AuthTokenResponse(
        String userId,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
