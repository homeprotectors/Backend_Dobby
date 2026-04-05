package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.dto.notification.DailyReminderDispatchSummary;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.service.NotificationDispatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationDispatchService notificationDispatchService;

    @Value("${app.push.manual-dispatch.enabled:false}")
    private boolean manualDispatchEnabled;

    @Value("${app.push.manual-dispatch.secret:}")
    private String manualDispatchSecret;

    @Operation(
            summary = "수동 알림 발송",
            description = "Immediately dispatch the daily chore reminder flow for testing"
    )
    @SecurityRequirements
    @PostMapping("/daily-chore-reminder/dispatch")
    public ResponseEntity<ResponseDTO<DailyReminderDispatchSummary>> dispatchDailyChoreReminder(
            @RequestHeader("X-ADMIN-SECRET") String adminSecret
    ) {
        validateManualDispatchAccess(adminSecret);

        DailyReminderDispatchSummary summary = notificationDispatchService.dispatchDailyChoreReminders();
        return ResponseEntity.ok(new ResponseDTO<>(true, "Daily chore reminder dispatched", summary));
    }

    @Operation(
            summary = "테스트용 중복 무시 푸시 발송",
            description = "Immediately dispatch the daily chore reminder flow for testing without checking today's delivery log"
    )
    @SecurityRequirements
    @PostMapping("/daily-chore-reminder/test-dispatch")
    public ResponseEntity<ResponseDTO<DailyReminderDispatchSummary>> testDispatchDailyChoreReminder(
            @RequestHeader("X-ADMIN-SECRET") String adminSecret
    ) {
        validateManualDispatchAccess(adminSecret);

        DailyReminderDispatchSummary summary = notificationDispatchService.dispatchDailyChoreReminders(true);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Daily chore reminder test-dispatched", summary));
    }

    private void validateManualDispatchAccess(String adminSecret) {
        // Keep this endpoint disabled by default because the project has no admin auth flow yet.
        if (!manualDispatchEnabled) {
            throw new ApiException("MANUAL_DISPATCH_DISABLED", "Manual dispatch endpoint is disabled.");
        }

        if (manualDispatchSecret == null || manualDispatchSecret.isBlank()) {
            throw new ApiException("MANUAL_DISPATCH_SECRET_NOT_SET", "Manual dispatch secret is not configured.");
        }

        if (!manualDispatchSecret.equals(adminSecret)) {
            throw new ApiException("INVALID_ADMIN_SECRET", "Invalid admin secret.");
        }
    }
}
