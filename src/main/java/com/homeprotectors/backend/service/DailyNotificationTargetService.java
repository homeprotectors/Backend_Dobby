package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.DailyChoreReminderTarget;
import com.homeprotectors.backend.entity.DeviceToken;
import com.homeprotectors.backend.entity.NotificationType;
import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import com.homeprotectors.backend.repository.NotificationDeliveryLogRepository;
import com.homeprotectors.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyNotificationTargetService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final NotificationType NOTIFICATION_TYPE = NotificationType.DAILY_CHORE_REMINDER;

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserRepository userRepository;
    private final ChoreRepository choreRepository;
    private final NotificationDeliveryLogRepository notificationDeliveryLogRepository;

    public List<DailyChoreReminderTarget> getDailyChoreReminderTargets() {
        LocalDate today = LocalDate.now(KST);
        OffsetDateTime threshold = OffsetDateTime.now(KST).minusDays(30);

        List<DeviceToken> activeTokens = deviceTokenRepository.findByEnabledTrueAndLastSeenAtAfter(threshold);
        if (activeTokens.isEmpty()) {
            return List.of();
        }

        Map<Long, List<DeviceToken>> tokensByUserId = activeTokens.stream()
                .collect(Collectors.groupingBy(DeviceToken::getUserId, LinkedHashMap::new, Collectors.toList()));

        List<User> users = userRepository.findAllById(tokensByUserId.keySet()).stream()
                .filter(user -> user.getGroupId() != null)
                .sorted(Comparator.comparing(User::getId))
                .toList();

        Map<Long, Long> choreCountByGroupId = new LinkedHashMap<>();
        List<DailyChoreReminderTarget> targets = new ArrayList<>();

        for (User user : users) {
            if (notificationDeliveryLogRepository.existsByUserIdAndNotificationTypeAndDeliveryDate(
                    user.getId(),
                    NOTIFICATION_TYPE,
                    today
            )) {
                continue;
            }

            Long groupId = user.getGroupId();
            long choreCount = choreCountByGroupId.computeIfAbsent(
                    groupId,
                    key -> choreRepository.countByGroupIdAndNextDueLessThanEqual(key, today)
            );

            if (choreCount <= 0) {
                continue;
            }

            List<String> pushTokens = tokensByUserId.getOrDefault(user.getId(), List.of()).stream()
                    .map(DeviceToken::getPushToken)
                    .distinct()
                    .toList();

            if (pushTokens.isEmpty()) {
                continue;
            }

            targets.add(new DailyChoreReminderTarget(
                    user.getId(),
                    pushTokens,
                    choreCount,
                    "오늘 할 일 알림",
                    "오늘 할 일 " + choreCount + "건 확인하세요"
            ));
        }

        return targets;
    }
}
