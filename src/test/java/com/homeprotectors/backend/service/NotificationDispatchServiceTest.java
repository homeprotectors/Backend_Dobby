package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.DailyChoreReminderTarget;
import com.homeprotectors.backend.dto.notification.DailyReminderDispatchSummary;
import com.homeprotectors.backend.dto.notification.PushNotificationResult;
import com.homeprotectors.backend.entity.DevicePlatform;
import com.homeprotectors.backend.entity.DeviceToken;
import com.homeprotectors.backend.entity.NotificationDeliveryLog;
import com.homeprotectors.backend.entity.NotificationDeliveryStatus;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import com.homeprotectors.backend.repository.NotificationDeliveryLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private DailyNotificationTargetService dailyNotificationTargetService;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private NotificationDeliveryLogRepository notificationDeliveryLogRepository;

    @Mock
    private DeviceTokenRepository deviceTokenRepository;

    @InjectMocks
    private NotificationDispatchService notificationDispatchService;

    @Test
    void dispatchDailyChoreReminders_savesLogAndDisablesInvalidTokens() {
        DailyChoreReminderTarget target = new DailyChoreReminderTarget(
                10L,
                List.of("token-1", "token-2"),
                2L,
                "오늘 할 일 알림",
                "오늘 할 일 2건 확인하세요"
        );

        DeviceToken invalidToken = new DeviceToken();
        invalidToken.setId(1L);
        invalidToken.setUserId(10L);
        invalidToken.setPlatform(DevicePlatform.IOS);
        invalidToken.setPushToken("token-2");
        invalidToken.setEnabled(true);

        when(dailyNotificationTargetService.getDailyChoreReminderTargets()).thenReturn(List.of(target));
        when(pushNotificationService.send(any()))
                .thenReturn(new PushNotificationResult(1, 1, List.of("token-2"), List.of("token-2")));
        when(deviceTokenRepository.findByPushTokenIn(List.of("token-2")))
                .thenReturn(List.of(invalidToken));

        DailyReminderDispatchSummary summary = notificationDispatchService.dispatchDailyChoreReminders();

        assertThat(summary.targetUserCount()).isEqualTo(1);
        assertThat(summary.successCount()).isEqualTo(1);
        assertThat(summary.failureCount()).isEqualTo(1);
        assertThat(summary.items()).hasSize(1);
        assertThat(summary.items().getFirst().invalidTokens()).containsExactly("token-2");

        ArgumentCaptor<NotificationDeliveryLog> logCaptor = ArgumentCaptor.forClass(NotificationDeliveryLog.class);
        verify(notificationDeliveryLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getUserId()).isEqualTo(10L);
        assertThat(logCaptor.getValue().getStatus()).isEqualTo(NotificationDeliveryStatus.SENT);
        assertThat(logCaptor.getValue().getBody()).isEqualTo("오늘 할 일 2건 확인하세요");

        verify(deviceTokenRepository).saveAll(any());
        assertThat(invalidToken.getEnabled()).isFalse();
    }
}
