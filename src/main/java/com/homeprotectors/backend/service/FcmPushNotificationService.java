package com.homeprotectors.backend.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import com.homeprotectors.backend.dto.notification.PushNotificationCommand;
import com.homeprotectors.backend.dto.notification.PushNotificationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class FcmPushNotificationService implements PushNotificationService {

    private static final int MAX_BATCH_SIZE = 500;

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public PushNotificationResult send(PushNotificationCommand command) {
        List<String> tokens = normalizeTokens(command.pushTokens());
        if (tokens.isEmpty()) {
            return new PushNotificationResult(0, 0, List.of(), List.of());
        }

        int successCount = 0;
        List<String> invalidTokens = new ArrayList<>();
        List<String> failedTokens = new ArrayList<>();

        for (int start = 0; start < tokens.size(); start += MAX_BATCH_SIZE) {
            int end = Math.min(start + MAX_BATCH_SIZE, tokens.size());
            List<String> batchTokens = tokens.subList(start, end);

            try {
                List<Message> messages = buildMessages(batchTokens, command.title(), command.body(), command.data());
                BatchResponse response = firebaseMessaging.sendEach(messages);
                successCount += response.getSuccessCount();

                for (int i = 0; i < response.getResponses().size(); i++) {
                    SendResponse sendResponse = response.getResponses().get(i);
                    String token = batchTokens.get(i);
                    if (!sendResponse.isSuccessful()) {
                        failedTokens.add(token);
                        FirebaseMessagingException exception = sendResponse.getException();
                        log.warn(
                                "FCM send failed for token. tokenSuffix={}, errorCode={}, messagingErrorCode={}, message={}",
                                maskToken(token),
                                exception == null ? null : exception.getErrorCode(),
                                exception == null ? null : exception.getMessagingErrorCode(),
                                exception == null ? null : exception.getMessage()
                        );
                        if (isInvalidToken(sendResponse.getException())) {
                            invalidTokens.add(token);
                        }
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.error(
                        "FCM batch send failed. batchSize={}, errorCode={}, messagingErrorCode={}, message={}",
                        batchTokens.size(),
                        e.getErrorCode(),
                        e.getMessagingErrorCode(),
                        e.getMessage(),
                        e
                );
                failedTokens.addAll(batchTokens);
            }
        }

        return new PushNotificationResult(
                successCount,
                failedTokens.size(),
                distinct(invalidTokens),
                distinct(failedTokens)
        );
    }

    private List<Message> buildMessages(
            List<String> tokens,
            String title,
            String body,
            Map<String, String> data
    ) {
        List<Message> messages = new ArrayList<>(tokens.size());
        for (String token : tokens) {
            Message.Builder builder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                builder.putAllData(data);
            }

            messages.add(builder.build());
        }
        return messages;
    }

    private List<String> normalizeTokens(List<String> pushTokens) {
        if (pushTokens == null || pushTokens.isEmpty()) {
            return List.of();
        }

        return pushTokens.stream()
                .filter(token -> token != null && !token.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private boolean isInvalidToken(FirebaseMessagingException exception) {
        return exception != null && exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED;
    }

    private List<String> distinct(List<String> values) {
        return new ArrayList<>(new LinkedHashSet<>(values));
    }

    private String maskToken(String token) {
        if (token == null || token.isBlank()) {
            return "(empty)";
        }

        String trimmed = token.trim();
        int visibleLength = Math.min(8, trimmed.length());
        return trimmed.substring(trimmed.length() - visibleLength);
    }
}
