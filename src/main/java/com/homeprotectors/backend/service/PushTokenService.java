package com.homeprotectors.backend.service;

import com.homeprotectors.backend.dto.notification.PushTokenRegisterRequest;
import com.homeprotectors.backend.dto.notification.PushTokenResponse;
import com.homeprotectors.backend.entity.DeviceToken;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PushTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserContextService userContextService;

    @Transactional
    public PushTokenResponse registerToken(PushTokenRegisterRequest request, UUID currentUserId) {
        Long userId = userContextService.requireInternalUserId(currentUserId);
        String normalizedPushToken = request.pushToken().trim();
        OffsetDateTime now = OffsetDateTime.now();

        DeviceToken token = deviceTokenRepository.findByPushToken(normalizedPushToken)
                .orElseGet(DeviceToken::new);

        token.setUserId(userId);
        token.setPlatform(request.platform());
        token.setPushToken(normalizedPushToken);
        token.setEnabled(true);
        token.setLastSeenAt(now);

        DeviceToken saved = deviceTokenRepository.save(token);
        return new PushTokenResponse(saved.getId(), saved.getPlatform(), Boolean.TRUE.equals(saved.getEnabled()));
    }

    @Transactional
    public void deleteToken(Long tokenId, UUID currentUserId) {
        Long userId = userContextService.requireInternalUserId(currentUserId);
        DeviceToken token = deviceTokenRepository.findByIdAndUserId(tokenId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Push token not found"));

        deviceTokenRepository.delete(token);
    }
}
