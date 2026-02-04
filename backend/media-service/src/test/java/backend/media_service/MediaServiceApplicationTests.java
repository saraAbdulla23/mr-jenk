package backend.media_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = MediaServiceApplication.class)
@ActiveProfiles("test")
class MediaServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
