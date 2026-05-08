package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.entity.DevicePlatform;
import com.homeprotectors.backend.entity.DeviceToken;
import com.homeprotectors.backend.entity.Group;
import com.homeprotectors.backend.entity.User;
import com.homeprotectors.backend.repository.DeviceTokenRepository;
import com.homeprotectors.backend.repository.GroupRepository;
import com.homeprotectors.backend.repository.UserRepository;
import com.homeprotectors.backend.service.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PushTokenControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DeviceTokenRepository deviceTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @MockBean
    private JwtTokenService jwtTokenService;

    @AfterEach
    void tearDown() {
        deviceTokenRepository.deleteAll();
        userRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    void updateTokenEnabled_updatesDeviceTokenEnabledInDatabase() throws Exception {
        Group group = groupRepository.save(new Group(null, null, "My Home", "INVITE11", null, null));
        User user = userRepository.saveAndFlush(new User(UUID.randomUUID(), UUID.randomUUID(), group.getId()));

        DeviceToken token = new DeviceToken();
        token.setUserId(user.getId());
        token.setPlatform(DevicePlatform.IOS);
        token.setPushToken("integration-token-1");
        token.setEnabled(true);
        token.setLastSeenAt(OffsetDateTime.now());
        token = deviceTokenRepository.saveAndFlush(token);

        mockMvc.perform(patch("/api/push-tokens/me/enabled")
                        .requestAttr("currentUserId", user.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pushToken": "integration-token-1",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(token.getId()))
                .andExpect(jsonPath("$.data.enabled").value(false));

        DeviceToken updated = deviceTokenRepository.findById(token.getId()).orElseThrow();
        assertThat(updated.getEnabled()).isFalse();
    }

    @Test
    void updateTokenEnabled_returnsNotFoundForOtherUsersToken() throws Exception {
        Group group = groupRepository.save(new Group(null, null, "Shared Home", "INVITE12", null, null));
        User owner = userRepository.saveAndFlush(new User(UUID.randomUUID(), UUID.randomUUID(), group.getId()));
        User anotherUser = userRepository.saveAndFlush(new User(UUID.randomUUID(), UUID.randomUUID(), group.getId()));

        DeviceToken token = new DeviceToken();
        token.setUserId(owner.getId());
        token.setPlatform(DevicePlatform.ANDROID);
        token.setPushToken("integration-token-2");
        token.setEnabled(true);
        token.setLastSeenAt(OffsetDateTime.now());
        token = deviceTokenRepository.saveAndFlush(token);

        mockMvc.perform(patch("/api/push-tokens/me/enabled")
                        .requestAttr("currentUserId", anotherUser.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "pushToken": "integration-token-2",
                                  "enabled": false
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Push token not found"));

        DeviceToken unchanged = deviceTokenRepository.findById(token.getId()).orElseThrow();
        assertThat(unchanged.getEnabled()).isTrue();
    }
}
