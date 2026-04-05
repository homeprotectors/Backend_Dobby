package com.homeprotectors.backend.config;

import com.google.firebase.messaging.FirebaseMessaging;
import com.homeprotectors.backend.service.DisabledPushNotificationService;
import com.homeprotectors.backend.service.FcmPushNotificationService;
import com.homeprotectors.backend.service.PushNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PushNotificationConfig {

    @Bean
    public PushNotificationService pushNotificationService(ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging != null) {
            log.info("Using FcmPushNotificationService for push delivery");
            return new FcmPushNotificationService(firebaseMessaging);
        }

        log.warn("Using DisabledPushNotificationService because FirebaseMessaging bean is unavailable");
        return new DisabledPushNotificationService();
    }
}
