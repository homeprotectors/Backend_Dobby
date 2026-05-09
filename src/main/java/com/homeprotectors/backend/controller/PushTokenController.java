package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.dto.notification.PushTokenEnabledUpdateRequest;
import com.homeprotectors.backend.dto.notification.PushTokenRegisterRequest;
import com.homeprotectors.backend.dto.notification.PushTokenResponse;
import com.homeprotectors.backend.service.PushTokenService;
import com.homeprotectors.backend.service.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/push-tokens")
public class PushTokenController {

    private final PushTokenService pushTokenService;
    private final UserContextService userContextService;

    @ModelAttribute
    public void loadCurrentUser(@RequestAttribute("currentUserId") UUID currentUserId) {
        userContextService.requireInternalUserId(currentUserId);
    }

    @Operation(summary = "Push token 등록", description = "Register or refresh the current user's push token")
    @PostMapping
    public ResponseEntity<ResponseDTO<PushTokenResponse>> registerToken(
            @Valid @RequestBody PushTokenRegisterRequest request,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        PushTokenResponse response = pushTokenService.registerToken(request, currentUserId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ResponseDTO<>(true, "Push token registered successfully", response));
    }

    @Operation(summary = "Push token 설정 조회", description = "Get the current user's push token notification setting")
    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<PushTokenResponse>> getToken(
            @RequestParam String pushToken,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        PushTokenResponse response = pushTokenService.getToken(pushToken, currentUserId);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Push token fetched successfully", response));
    }

    @Operation(summary = "Push token 삭제", description = "Delete one of the current user's push tokens")
    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Void> deleteToken(
            @PathVariable Long tokenId,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        pushTokenService.deleteToken(tokenId, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Push token 알림 on/off", description = "Enable or disable push notifications for the current user's push token")
    @PatchMapping("/me/enabled")
    public ResponseEntity<ResponseDTO<PushTokenResponse>> updateTokenEnabled(
            @Valid @RequestBody PushTokenEnabledUpdateRequest request,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        PushTokenResponse response = pushTokenService.updateTokenEnabled(request, currentUserId);
        return ResponseEntity.ok(new ResponseDTO<>(true, "Push token notification setting updated", response));
    }
}
