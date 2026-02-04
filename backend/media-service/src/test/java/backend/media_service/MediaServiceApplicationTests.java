package backend.media_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(classes = MediaServiceApplication.class)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
class MediaServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}
