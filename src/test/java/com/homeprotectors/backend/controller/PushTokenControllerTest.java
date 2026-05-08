package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.dto.notification.PushTokenResponse;
import com.homeprotectors.backend.entity.DevicePlatform;
import com.homeprotectors.backend.service.JwtTokenService;
import com.homeprotectors.backend.service.PushTokenService;
import com.homeprotectors.backend.service.UserContextService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PushTokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PushTokenService pushTokenService;

    @MockBean
    private UserContextService userContextService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void updateTokenEnabled_returnsUpdatedState() throws Exception {
        UUID currentUserId = UUID.randomUUID();
        Long tokenId = 10L;

        given(userContextService.requireInternalUserId(eq(currentUserId))).willReturn(1L);
        given(pushTokenService.updateTokenEnabled(
                eq(tokenId),
                org.mockito.ArgumentMatchers.any(),
                eq(currentUserId)
        )).willReturn(new PushTokenResponse(tokenId, DevicePlatform.IOS, false));

        mockMvc.perform(patch("/api/push-tokens/{tokenId}/enabled", tokenId)
                        .requestAttr("currentUserId", currentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.platform").value("IOS"))
                .andExpect(jsonPath("$.data.enabled").value(false));
    }
}
