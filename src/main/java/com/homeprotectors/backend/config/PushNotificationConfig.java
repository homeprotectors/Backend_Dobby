package com.homeprotectors.backend.config;

import com.google.firebase.messaging.FirebaseMessaging;
import com.homeprotectors.backend.service.DisabledPushNotificationService;
import com.homeprotectors.backend.service.FcmPushNotificationService;
import com.homeprotectors.backend.service.PushNotificationService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PushNotificationConfig {

    @Bean
    public PushNotificationService pushNotificationService(ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging != null) {
            return new FcmPushNotificationService(firebaseMessaging);
        }

        return new DisabledPushNotificationService();
    }
}
