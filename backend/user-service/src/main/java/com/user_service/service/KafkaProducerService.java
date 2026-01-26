package com.user_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_service.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kafka.topic.user-events}")
    private String userEventsTopic;

    public void sendUserEvent(String eventType, User user) {
        try {
            // Include ID in message
            String message = String.format(
                "{\"eventType\":\"%s\", \"user\":{\"id\":\"%s\", \"email\":\"%s\", \"name\":\"%s\", \"role\":\"%s\"}}",
                eventType, user.getId(), user.getEmail(), user.getName(), user.getRole()
            );
            kafkaTemplate.send(userEventsTopic, message);
            System.out.println("✅ Sent Kafka event: " + message);
        } catch (Exception e) {
            System.err.println("❌ Failed to send Kafka message: " + e.getMessage());
        }
    }
}
