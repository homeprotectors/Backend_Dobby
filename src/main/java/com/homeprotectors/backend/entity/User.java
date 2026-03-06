package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_install_id", columnNames = "install_id"),
                @UniqueConstraint(name = "uk_users_public_id", columnNames = "public_id")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "install_id", nullable = false, unique = true, updatable = false)
    private UUID installId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected User() {}

    public User(UUID publicId, UUID installId, Long groupId) {
        this.publicId = publicId;
        this.installId = installId;
        this.groupId = groupId;
    }

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public UUID getPublicId() { return publicId; }
    public UUID getInstallId() { return installId; }
    public Long getGroupId() { return groupId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
