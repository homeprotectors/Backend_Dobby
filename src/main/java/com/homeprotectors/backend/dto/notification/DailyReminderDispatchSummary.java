package com.homeprotectors.backend.dto.notification;

import java.util.List;

public record DailyReminderDispatchSummary(
        int targetUserCount,
        int successCount,
        int failureCount,
        List<DailyReminderDispatchItem> items
) {}
