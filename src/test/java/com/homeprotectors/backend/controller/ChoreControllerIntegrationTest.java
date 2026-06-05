package com.homeprotectors.backend.controller;

import com.homeprotectors.backend.entity.Group;
import com.homeprotectors.backend.entity.User;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ChoreControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @MockBean
    private JwtTokenService jwtTokenService;

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("delete from chores");
        userRepository.deleteAll();
        groupRepository.deleteAll();
    }

    @Test
    void createChore_allowsEmptySelectedCycleForPerTwoWeeks() throws Exception {
        Group group = groupRepository.save(new Group(null, null, "My Home", "INVITE21", null, null));
        User user = userRepository.saveAndFlush(new User(UUID.randomUUID(), UUID.randomUUID(), group.getId()));

        mockMvc.perform(post("/api/chores")
                        .requestAttr("currentUserId", user.getPublicId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Vacuum",
                                  "recurrenceType": "PER_2WEEKS",
                                  "selectedCycle": [],
                                  "roomCategory": "ETC"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.recurrenceType").value("PER_2WEEKS"))
                .andExpect(jsonPath("$.data.selectedCycle").isArray())
                .andExpect(jsonPath("$.data.selectedCycle").isEmpty());
    }
}
