package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.common.GuestRegisterRequest;
import com.homeprotectors.backend.dto.common.GuestRegisterResponse;
import com.homeprotectors.backend.exception.ApiException;
import com.homeprotectors.backend.service.GuestRegisterService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(path = "/api/guests", produces = MediaType.APPLICATION_JSON_VALUE)
public class GuestRegisterController {

    private final GuestRegisterService guestRegisterService;

    public GuestRegisterController(GuestRegisterService guestRegisterService) {
        this.guestRegisterService = guestRegisterService;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public GuestRegisterResponse register(@Valid @RequestBody GuestRegisterRequest request) {
        UUID installId = parseUuidOrThrow(request.installId(), "installId");
        UUID userId = guestRegisterService.registerGuest(installId);
        return new GuestRegisterResponse(userId.toString());
    }

    private UUID parseUuidOrThrow(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            throw new ApiException("INVALID_UUID", fieldName + " must be a valid UUID.");
        }
    }
}
