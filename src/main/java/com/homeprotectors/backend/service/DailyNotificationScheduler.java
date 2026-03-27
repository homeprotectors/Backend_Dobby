package com.homeprotectors.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "true")
public class DailyNotificationScheduler {

    private final NotificationDispatchService notificationDispatchService;

    // Run once every morning in KST and delegate the full send flow to the dispatch service.
    @Scheduled(
            cron = "${app.push.daily-chore-reminder.cron}",
            zone = "${app.push.daily-chore-reminder.zone}"
    )
    public void dispatchDailyChoreReminders() {
        var summary = notificationDispatchService.dispatchDailyChoreReminders();
        log.info(
                "Daily chore reminder dispatch finished. targetUsers={}, successCount={}, failureCount={}",
                summary.targetUserCount(),
                summary.successCount(),
                summary.failureCount()
        );
    }
}
