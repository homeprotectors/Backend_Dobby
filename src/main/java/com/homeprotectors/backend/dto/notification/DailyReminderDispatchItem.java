package com.homeprotectors.backend.dto.notification;

import java.util.List;

public record DailyReminderDispatchItem(
        Long userId,
        long choreCount,
        int successCount,
        int failureCount,
        List<String> invalidTokens
) {}
