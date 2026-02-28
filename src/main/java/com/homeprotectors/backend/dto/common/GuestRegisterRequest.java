package com.homeprotectors.backend.dto.common;

import jakarta.validation.constraints.NotBlank;

public record GuestRegisterRequest(
        @NotBlank(message = "installId is required")
        String installId
) {}
