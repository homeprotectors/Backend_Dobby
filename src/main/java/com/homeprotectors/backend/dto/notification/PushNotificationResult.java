package com.homeprotectors.backend.dto.notification;

import java.util.List;

public record PushNotificationResult(
        int successCount,
        int failureCount,
        List<String> invalidTokens,
        List<String> failedTokens
) {}
