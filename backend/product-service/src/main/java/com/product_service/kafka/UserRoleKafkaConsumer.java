package com.product_service.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class UserRoleKafkaConsumer {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Consumes user role events for logging / internal analytics.
     * Authorization is now fully handled via JWT claims.
     */
    @KafkaListener(topics = "${kafka.topic.user-events}", groupId = "product-service-group")
    public void consume(String message) {
        try {
            JsonNode root = mapper.readTree(message);
            if (root.has("user")) {
                JsonNode userNode = root.get("user");
                if (userNode.has("id") && userNode.has("role")) {
                    String userId = userNode.get("id").asText();
                    String role = userNode.get("role").asText().replace("ROLE_", "");
                    System.out.println("📢 User role event received: " + userId + " -> " + role);
                } else {
                    System.err.println("⚠️ Missing id or role in message: " + message);
                }
            } else {
                System.err.println("⚠️ No 'user' field in message: " + message);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to parse Kafka message: " + message);
        }
    }
}
