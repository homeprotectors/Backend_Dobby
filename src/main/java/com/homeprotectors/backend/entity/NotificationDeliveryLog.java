package com.homeprotectors.backend.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(
        name = "notification_delivery_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_delivery_once_per_day",
                        columnNames = {"user_id", "notification_type", "delivery_date"}
                )
        },
        indexes = {
                @Index(name = "idx_notification_delivery_lookup", columnList = "user_id, notification_type, delivery_date")
        }
)
@Data
public class NotificationDeliveryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationDeliveryStatus status;

    @Column(length = 120)
    private String title;

    @Column(length = 255)
    private String body;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
