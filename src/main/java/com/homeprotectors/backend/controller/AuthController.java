package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.auth.AuthTokenResponse;
import com.homeprotectors.backend.dto.auth.RefreshTokenRequest;
import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.service.JwtTokenService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final JwtTokenService jwtTokenService;

    @PostMapping(path = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirements
    public ResponseEntity<ResponseDTO<AuthTokenResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthTokenResponse response = jwtTokenService.refresh(request.refreshToken());
        return ResponseEntity.ok(new ResponseDTO<>(true, "Token refreshed successfully", response));
    }
}
