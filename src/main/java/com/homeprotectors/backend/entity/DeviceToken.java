package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "device_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_device_tokens_push_token", columnNames = "push_token")
        },
        indexes = {
                @Index(name = "idx_device_tokens_user_enabled", columnList = "user_id, enabled"),
                @Index(name = "idx_device_tokens_last_seen", columnList = "last_seen_at")
        }
)
@Data
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "push_token", nullable = false, unique = true, length = 512)
    private String pushToken;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "last_seen_at", nullable = false)
    private OffsetDateTime lastSeenAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        if (enabled == null) {
            enabled = true;
        }
        if (lastSeenAt == null) {
            lastSeenAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
