package com.product_service.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_service.model.Product;
import com.product_service.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class MediaKafkaConsumer {

    private final ProductService productService;
    private final ObjectMapper mapper = new ObjectMapper();

    public MediaKafkaConsumer(ProductService productService) {
        this.productService = productService;
    }

    // Listen for media uploads
    @KafkaListener(topics = "media-uploaded-topic", groupId = "product-service-group")
    public void consumeMediaUploaded(String message) {
        try {
            JsonNode root = mapper.readTree(message);
            String productId = root.get("productId").asText();
            String userId = root.get("userId").asText();
            String imageUrl = root.get("imageUrl").asText();

            System.out.println("📥 Media uploaded event received: " + message);

            // Add image URL to product
            Product updated = productService.addImage(productId, userId, imageUrl);
            System.out.println("✅ Image URL added to product " + productId + ": " + imageUrl);

        } catch (Exception e) {
            System.err.println("❌ Failed to process media-uploaded event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Listen for media deletions
    @KafkaListener(topics = "media-deleted-topic", groupId = "product-service-group")
    public void consumeMediaDeleted(String message) {
        try {
            JsonNode root = mapper.readTree(message);
            String productId = root.get("productId").asText();
            String userId = root.get("userId").asText();
            String imageUrl = root.get("imageUrl").asText();

            System.out.println("📥 Media deleted event received: " + message);

            // Remove image URL from product
            Product updated = productService.removeImage(productId, userId, imageUrl);
            System.out.println("✅ Image URL removed from product " + productId + ": " + imageUrl);

        } catch (Exception e) {
            System.err.println("❌ Failed to process media-deleted event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
