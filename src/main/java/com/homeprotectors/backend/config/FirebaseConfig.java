package com.homeprotectors.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "true")
    public FirebaseApp firebaseApp(
            @Value("${app.push.fcm.credentials-path:}") String credentialsPath,
            @Value("${app.push.fcm.project-id:}") String projectId
    ) throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }

        FirebaseOptions.Builder builder = FirebaseOptions.builder();

        // When a service account file path is provided, load it directly.
        // Otherwise fall back to application default credentials.
        if (credentialsPath != null && !credentialsPath.isBlank()) {
            try (InputStream inputStream = new FileInputStream(credentialsPath)) {
                builder.setCredentials(GoogleCredentials.fromStream(inputStream));
            }
        } else {
            builder.setCredentials(GoogleCredentials.getApplicationDefault());
        }

        if (projectId != null && !projectId.isBlank()) {
            builder.setProjectId(projectId);
        }

        return FirebaseApp.initializeApp(builder.build());
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.push", name = "enabled", havingValue = "true")
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
