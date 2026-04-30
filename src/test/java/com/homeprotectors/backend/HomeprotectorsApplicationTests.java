package com.homeprotectors.backend;

import com.homeprotectors.backend.service.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HomeprotectorsApplicationTests {

    @MockBean
    private JwtTokenService jwtTokenService;

    @Test
    void contextLoads() {
    }
}
