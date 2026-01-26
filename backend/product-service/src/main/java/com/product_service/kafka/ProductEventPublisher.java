package com.product_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_service.model.Product;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProductEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProductEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishProductCreated(Product product) {
        try {
            String message = objectMapper.writeValueAsString(product);
            kafkaTemplate.send("product-created-topic", message);
        } catch (Exception e) {
            System.err.println("Failed to publish ProductCreatedEvent: " + e.getMessage());
        }
    }

    public void publishProductUpdated(Product product) {
        try {
            String message = objectMapper.writeValueAsString(product);
            kafkaTemplate.send("product-updated-topic", message);
        } catch (Exception e) {
            System.err.println("Failed to publish ProductUpdatedEvent: " + e.getMessage());
        }
    }

    public void publishProductDeleted(Product product) {
        try {
            String message = objectMapper.writeValueAsString(product);
            kafkaTemplate.send("product-deleted-topic", message);
        } catch (Exception e) {
            System.err.println("Failed to publish ProductDeletedEvent: " + e.getMessage());
        }
    }
}
