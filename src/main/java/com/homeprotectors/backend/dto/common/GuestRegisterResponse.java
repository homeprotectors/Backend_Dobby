package com.homeprotectors.backend.dto.common;

import com.homeprotectors.backend.dto.auth.AuthTokenResponse;

public record GuestRegisterResponse(String userId, AuthTokenResponse tokens) {}
