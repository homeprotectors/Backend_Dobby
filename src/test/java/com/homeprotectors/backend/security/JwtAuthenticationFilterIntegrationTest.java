package com.homeprotectors.backend.security;

import com.homeprotectors.backend.service.JwtTokenService;
import com.homeprotectors.backend.service.NotificationDispatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JwtAuthenticationFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtTokenService jwtTokenService;

    @MockBean
    private NotificationDispatchService notificationDispatchService;

    @Test
    void adminNotificationDispatch_requiresAuthorizationHeader() throws Exception {
        mockMvc.perform(post("/api/admin/notifications/daily-chore-reminder/dispatch"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Authorization header is required."));
    }
}
