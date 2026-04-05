package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.PushNotificationCommand;
import com.homeprotectors.backend.dto.notification.PushNotificationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DisabledPushNotificationService implements PushNotificationService {

    @Override
    public PushNotificationResult send(PushNotificationCommand command) {
        log.warn("Push delivery is disabled. Returning failed result for {} tokens.", command.pushTokens() == null ? 0 : command.pushTokens().size());
        List<String> failedTokens = command.pushTokens() == null ? List.of() : command.pushTokens();
        return new PushNotificationResult(0, failedTokens.size(), List.of(), failedTokens);
    }
}
