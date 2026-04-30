package com.homeprotectors.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String iss,
        String aud,
        long accessTtlMin,
        long refreshTtlDays,
        String privateKeyPemPath,
        String publicKeyPemPath
) {
}
