package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.PushNotificationCommand;
import com.homeprotectors.backend.dto.notification.PushNotificationResult;

public interface PushNotificationService {
    PushNotificationResult send(PushNotificationCommand command);
}
