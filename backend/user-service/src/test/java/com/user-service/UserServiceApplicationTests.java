package com.user_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;

@SpringBootTest(classes = UserServiceApplication.class)
@ActiveProfiles("test")
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:0",
        "port=0"
    }
)
@EnableAutoConfiguration(exclude = {
    SecurityAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
class UserServiceApplicationTests {

    /**
     * Mocked to satisfy AuthService constructor
     * without enabling full Spring Security
     */
    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void contextLoads() {
        // verifies Spring context loads in CI
    }
}
