package com.homeprotectors.backend.dto.notification;

import com.homeprotectors.backend.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PushTokenRegisterRequest(
        @NotNull(message = "platform is required")
        DevicePlatform platform,

        @NotBlank(message = "pushToken is required")
        String pushToken
) {}
