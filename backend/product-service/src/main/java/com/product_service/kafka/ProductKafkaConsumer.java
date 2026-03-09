package com.product_service.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.product_service.model.Product;
import com.product_service.service.ProductService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProductKafkaConsumer {

    private final ProductService productService;
    private final ObjectMapper mapper = new ObjectMapper();

    public ProductKafkaConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "product-events", groupId = "product-service-group")
    public void consumeProductEvent(String message) {
        try {
            JsonNode root = mapper.readTree(message);

            String eventType = root.get("eventType").asText();
            String productId = root.get("productId").asText();
            int quantity = root.get("quantity").asInt();
            String orderId = root.get("orderId").asText();

            System.out.println("📥 Event received: " + eventType +
                    " | Product: " + productId +
                    " | Quantity: " + quantity +
                    " | Order: " + orderId);

            switch (eventType) {

                // 🔽 Decrease stock for new orders
                case "ORDER_CREATED":
                case "ORDER_REDO":

                    Product decreased = productService.decreaseStock(productId, quantity);

                    System.out.println("➖ Stock decreased. New quantity: "
                            + decreased.getQuantity());
                    break;

                // 🔼 Restore stock for cancelled orders
                case "ORDER_CANCELLED":

                    Product increased = productService.increaseStock(productId, quantity);

                    System.out.println("➕ Stock restored. New quantity: "
                            + increased.getQuantity());
                    break;

                default:
                    System.out.println("⏭ Ignored event type: " + eventType);
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to process product-event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}