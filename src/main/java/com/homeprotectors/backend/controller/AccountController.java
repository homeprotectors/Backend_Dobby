package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Delete current account", description = "Delete the current guest account and its personal data")
    @DeleteMapping
    public ResponseEntity<Void> deleteCurrentAccount(@RequestAttribute("currentUserId") UUID currentUserId) {
        accountService.deleteCurrentAccount(currentUserId);
        return ResponseEntity.noContent().build();
    }
}
