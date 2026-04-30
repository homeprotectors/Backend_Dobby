package com.homeprotectors.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
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

    private static final String DEFAULT_PROVIDER = "APPLE";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "apple_sub")
    private String appleSub;

    @Column(name = "email")
    private String email;

    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "install_id", nullable = false, unique = true, updatable = false)
    private UUID installId;

    @Column(name = "public_id", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "token_version", nullable = false)
    private Long tokenVersion;

    protected User() {
    }

    public User(UUID publicId, UUID installId, Long groupId) {
        this.publicId = publicId;
        this.installId = installId;
        this.groupId = groupId;
    }

    @PrePersist
    void onCreate() {
        if (this.provider == null || this.provider.isBlank()) {
            this.provider = DEFAULT_PROVIDER;
        }
        if (this.emailVerified == null) {
            this.emailVerified = false;
        }
        if (this.tokenVersion == null) {
            this.tokenVersion = 0L;
        }

        LocalDateTime createdNow = LocalDateTime.now();
        OffsetDateTime updatedNow = OffsetDateTime.now();

        if (this.createdAt == null) {
            this.createdAt = createdNow;
        }
        this.updatedAt = updatedNow;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getProvider() {
        return provider;
    }

    public String getAppleSub() {
        return appleSub;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public UUID getInstallId() {
        return installId;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getTokenVersion() {
        return tokenVersion;
    }

    public void incrementTokenVersion() {
        this.tokenVersion = this.tokenVersion == null ? 1L : this.tokenVersion + 1L;
    }
}
