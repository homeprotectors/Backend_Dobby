package com.homeprotectors.backend.dto.notification;

import java.util.List;

public record DailyChoreReminderTarget(
        Long userId,
        List<String> pushTokens,
        long choreCount,
        String title,
        String body
) {}
