package com.homeprotectors.backend.dto.notification;

import jakarta.validation.constraints.NotNull;

public record PushTokenEnabledUpdateRequest(
        @NotNull(message = "enabled is required")
        Boolean enabled
) {}
