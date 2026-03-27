package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.DailyChoreReminderTarget;
import com.homeprotectors.backend.dto.notification.DailyReminderDispatchItem;
import com.homeprotectors.backend.dto.notification.DailyReminderDispatchSummary;
import com.homeprotectors.backend.dto.notification.PushNotificationCommand;
import com.homeprotectors.backend.dto.notification.PushNotificationResult;
import com.homeprotectors.backend.entity.DeviceToken;
import com.homeprotectors.backend.entity.NotificationDeliveryLog;
import com.homeprotectors.backend.entity.NotificationDeliveryStatus;
import com.homeprotectors.backend.entity.NotificationType;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import com.homeprotectors.backend.repository.NotificationDeliveryLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final DailyNotificationTargetService dailyNotificationTargetService;
    private final PushNotificationService pushNotificationService;
    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;
    private final DeviceTokenRepository deviceTokenRepository;

    public DailyReminderDispatchSummary dispatchDailyChoreReminders() {
        LocalDate today = LocalDate.now(KST);
        List<DailyChoreReminderTarget> targets = dailyNotificationTargetService.getDailyChoreReminderTargets();
        List<DailyReminderDispatchItem> items = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (DailyChoreReminderTarget target : targets) {
            PushNotificationCommand command = new PushNotificationCommand(
                    target.pushTokens(),
                    target.title(),
                    target.body(),
                    Map.of(
                            "type", NotificationType.DAILY_CHORE_REMINDER.name(),
                            "date", today.toString(),
                            "choreCount", String.valueOf(target.choreCount())
                    )
            );

            PushNotificationResult result = pushNotificationService.send(command);
            saveDeliveryLog(target, result, today);
            disableInvalidTokens(result.invalidTokens());

            successCount += result.successCount();
            failureCount += result.failureCount();

            items.add(new DailyReminderDispatchItem(
                    target.userId(),
                    target.choreCount(),
                    result.successCount(),
                    result.failureCount(),
                    result.invalidTokens()
            ));
        }

        return new DailyReminderDispatchSummary(
                targets.size(),
                successCount,
                failureCount,
                items
        );
    }

    private void saveDeliveryLog(
            DailyChoreReminderTarget target,
            PushNotificationResult result,
            LocalDate deliveryDate
    ) {
        NotificationDeliveryLog log = new NotificationDeliveryLog();
        log.setUserId(target.userId());
        log.setNotificationType(NotificationType.DAILY_CHORE_REMINDER);
        log.setDeliveryDate(deliveryDate);
        log.setStatus(result.successCount() > 0 ? NotificationDeliveryStatus.SENT : NotificationDeliveryStatus.FAILED);
        log.setTitle(target.title());
        log.setBody(target.body());
        log.setErrorMessage(buildErrorMessage(result));
        notificationDeliveryLogRepository.save(log);
    }

    private String buildErrorMessage(PushNotificationResult result) {
        if (result.failureCount() <= 0) {
            return null;
        }

        if (!result.invalidTokens().isEmpty()) {
            return "Failed tokens: " + result.failureCount() + ", invalid tokens: " + result.invalidTokens().size();
        }

        return "Failed tokens: " + result.failureCount();
    }

    private void disableInvalidTokens(List<String> invalidTokens) {
        if (invalidTokens == null || invalidTokens.isEmpty()) {
            return;
        }

        List<DeviceToken> tokens = deviceTokenRepository.findByPushTokenIn(invalidTokens);
        OffsetDateTime now = OffsetDateTime.now(KST);

        for (DeviceToken token : tokens) {
            token.setEnabled(false);
            token.setUpdatedAt(now);
        }

        if (!tokens.isEmpty()) {
            deviceTokenRepository.saveAll(tokens);
        }
    }
}
