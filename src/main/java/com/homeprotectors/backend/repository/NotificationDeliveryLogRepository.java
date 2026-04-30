package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.NotificationDeliveryLog;
import com.homeprotectors.backend.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface NotificationDeliveryLogRepository extends JpaRepository<NotificationDeliveryLog, Long> {
    boolean existsByUserIdAndNotificationTypeAndDeliveryDate(
            Long userId,
            NotificationType notificationType,
            LocalDate deliveryDate
    );

    void deleteByUserId(Long userId);
}
