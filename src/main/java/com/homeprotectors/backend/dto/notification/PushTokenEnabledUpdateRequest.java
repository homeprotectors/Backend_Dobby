package com.homeprotectors.backend.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PushTokenEnabledUpdateRequest(
        @NotBlank(message = "pushToken is required")
        String pushToken,

        @NotNull(message = "enabled is required")
        Boolean enabled
) {}
