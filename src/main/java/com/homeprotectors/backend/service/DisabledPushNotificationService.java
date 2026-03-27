package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.PushNotificationCommand;
import com.homeprotectors.backend.dto.notification.PushNotificationResult;

import java.util.List;

public class DisabledPushNotificationService implements PushNotificationService {

    @Override
    public PushNotificationResult send(PushNotificationCommand command) {
        List<String> failedTokens = command.pushTokens() == null ? List.of() : command.pushTokens();
        return new PushNotificationResult(0, failedTokens.size(), List.of(), failedTokens);
    }
}
