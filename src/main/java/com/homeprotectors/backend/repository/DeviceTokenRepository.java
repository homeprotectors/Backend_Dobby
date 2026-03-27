package com.homeprotectors.backend.repository;

import com.homeprotectors.backend.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
    Optional<DeviceToken> findByPushToken(String pushToken);

    Optional<DeviceToken> findByIdAndUserId(Long id, Long userId);

    List<DeviceToken> findByUserIdAndEnabledTrue(Long userId);

    List<DeviceToken> findByEnabledTrueAndLastSeenAtAfter(OffsetDateTime threshold);

    List<DeviceToken> findByPushTokenIn(List<String> pushTokens);
}
