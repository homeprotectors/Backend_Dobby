package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.DailyChoreReminderTarget;
import com.homeprotectors.backend.entity.DevicePlatform;
import com.homeprotectors.backend.entity.DeviceToken;
import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.repository.ChoreRepository;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import com.homeprotectors.backend.repository.NotificationDeliveryLogRepository;
import com.homeprotectors.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyNotificationTargetServiceTest {

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChoreRepository choreRepository;

    @Mock
    private NotificationDeliveryLogRepository notificationDeliveryLogRepository;

    @Mock
    private PushNotificationTemplateProvider pushNotificationTemplateProvider;

    @InjectMocks
    private DailyNotificationTargetService dailyNotificationTargetService;

    @Test
    void getDailyChoreReminderTargets_returnsOnlyEligibleUsers() {
        DeviceToken token = new DeviceToken();
        token.setId(1L);
        token.setUserId(10L);
        token.setPlatform(DevicePlatform.ANDROID);
        token.setPushToken("token-1");
        token.setEnabled(true);
        token.setLastSeenAt(OffsetDateTime.now());

        DeviceToken duplicateToken = new DeviceToken();
        duplicateToken.setId(2L);
        duplicateToken.setUserId(10L);
        duplicateToken.setPlatform(DevicePlatform.ANDROID);
        duplicateToken.setPushToken("token-1");
        duplicateToken.setEnabled(true);
        duplicateToken.setLastSeenAt(OffsetDateTime.now());

        User eligibleUser = new User(UUID.randomUUID(), UUID.randomUUID(), 100L);
        setUserId(eligibleUser, 10L);

        User alreadySentUser = new User(UUID.randomUUID(), UUID.randomUUID(), 200L);
        setUserId(alreadySentUser, 20L);

        when(deviceTokenRepository.findByEnabledTrueAndLastSeenAtAfter(any()))
                .thenReturn(List.of(token, duplicateToken));
        when(userRepository.findAllById(any()))
                .thenReturn(List.of(eligibleUser, alreadySentUser));
        when(notificationDeliveryLogRepository.existsByUserIdAndNotificationTypeAndDeliveryDate(eq(10L), any(), any()))
                .thenReturn(false);
        when(notificationDeliveryLogRepository.existsByUserIdAndNotificationTypeAndDeliveryDate(eq(20L), any(), any()))
                .thenReturn(true);
        when(choreRepository.countByGroupIdAndNextDueLessThanEqual(eq(100L), any()))
                .thenReturn(3L);
        when(pushNotificationTemplateProvider.getDailyChoreReminderTitle())
                .thenReturn("✨ 좋은 아침이에요!");
        when(pushNotificationTemplateProvider.renderDailyChoreReminderBody(3L))
                .thenReturn("오늘 할 집안일이 3개 있어요. 앱에서 이번 주 할 일을 확인해보세요!");

        List<DailyChoreReminderTarget> targets = dailyNotificationTargetService.getDailyChoreReminderTargets();

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().userId()).isEqualTo(10L);
        assertThat(targets.getFirst().choreCount()).isEqualTo(3L);
        assertThat(targets.getFirst().pushTokens()).containsExactly("token-1");
        assertThat(targets.getFirst().title()).isEqualTo("✨ 좋은 아침이에요!");
        assertThat(targets.getFirst().body()).isEqualTo("오늘 할 집안일이 3개 있어요. 앱에서 이번 주 할 일을 확인해보세요!");
    }

    @Test
    void getDailyChoreReminderTargets_withIgnoreAlreadySentToday_keepsAlreadySentUsersEligible() {
        DeviceToken token = new DeviceToken();
        token.setId(1L);
        token.setUserId(20L);
        token.setPlatform(DevicePlatform.IOS);
        token.setPushToken("token-2");
        token.setEnabled(true);
        token.setLastSeenAt(OffsetDateTime.now());

        User alreadySentUser = new User(UUID.randomUUID(), UUID.randomUUID(), 200L);
        setUserId(alreadySentUser, 20L);

        when(deviceTokenRepository.findByEnabledTrueAndLastSeenAtAfter(any()))
                .thenReturn(List.of(token));
        when(userRepository.findAllById(any()))
                .thenReturn(List.of(alreadySentUser));
        when(choreRepository.countByGroupIdAndNextDueLessThanEqual(eq(200L), any()))
                .thenReturn(1L);
        when(pushNotificationTemplateProvider.getDailyChoreReminderTitle())
                .thenReturn("✨ 좋은 아침이에요!");
        when(pushNotificationTemplateProvider.renderDailyChoreReminderBody(1L))
                .thenReturn("오늘 할 집안일이 1개 있어요. 앱에서 이번 주 할 일을 확인해보세요!");

        List<DailyChoreReminderTarget> targets = dailyNotificationTargetService.getDailyChoreReminderTargets(true);

        assertThat(targets).hasSize(1);
        assertThat(targets.getFirst().userId()).isEqualTo(20L);
        assertThat(targets.getFirst().title()).isEqualTo("✨ 좋은 아침이에요!");
    }

    private void setUserId(User user, Long id) {
        try {
            var field = User.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(user, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
