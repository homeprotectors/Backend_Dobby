package com.homeprotectors.backend.dto.notification;

import java.util.List;
import java.util.Map;

public record PushNotificationCommand(
        List<String> pushTokens,
        String title,
        String body,
        Map<String, String> data
) {}
