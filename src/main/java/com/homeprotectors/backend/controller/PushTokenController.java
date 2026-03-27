package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.common.ResponseDTO;
import com.homeprotectors.backend.dto.notification.PushTokenRegisterRequest;
import com.homeprotectors.backend.dto.notification.PushTokenResponse;
import com.homeprotectors.backend.service.PushTokenService;
import com.homeprotectors.backend.service.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "Push token 삭제", description = "Delete one of the current user's push tokens")
    @DeleteMapping("/{tokenId}")
    public ResponseEntity<Void> deleteToken(
            @PathVariable Long tokenId,
            @RequestAttribute("currentUserId") UUID currentUserId) {
        pushTokenService.deleteToken(tokenId, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
