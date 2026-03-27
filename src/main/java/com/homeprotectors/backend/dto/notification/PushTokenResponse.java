package com.homeprotectors.backend.dto.notification;

import com.homeprotectors.backend.entity.DevicePlatform;

public record PushTokenResponse(
        Long id,
        DevicePlatform platform,
        boolean enabled
) {}
