package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.service.GuestRegisterService;
import com.homeprotectors.backend.service.JwtTokenService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class GuestRegisterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GuestRegisterService guestRegisterService;

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void register_returnsAccessAndRefreshTokens() throws Exception {
        UUID installId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        given(guestRegisterService.registerGuest(eq(installId))).willReturn(userId);
        given(jwtTokenService.issueTokens(eq(userId))).willReturn(
                new com.homeprotectors.backend.dto.auth.AuthTokenResponse(
                        userId.toString(),
                        "access-token-value",
                        "refresh-token-value",
                        "Bearer",
                        900L
                )
        );

        mockMvc.perform(post("/api/guests/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "installId": "%s"
                                }
                                """.formatted(installId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId.toString()))
                .andExpect(jsonPath("$.data.tokens.accessToken").value("access-token-value"))
                .andExpect(jsonPath("$.data.tokens.refreshToken").value("refresh-token-value"))
                .andExpect(jsonPath("$.data.tokens.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.tokens.expiresIn").value(900));
    }
}
