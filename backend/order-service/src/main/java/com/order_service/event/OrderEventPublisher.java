package com.order_service.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.order_service.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String ORDER_TOPIC = "order-events";
    private static final String PRODUCT_TOPIC = "product-events";

    public void publishEvent(String eventType, Order order) {
        try {
            // --------------------------
            // ORDER LEVEL EVENT
            // --------------------------
            Map<String, Object> orderEvent = new HashMap<>();
            orderEvent.put("eventType", eventType);
            orderEvent.put("orderId", order.getId());
            orderEvent.put("userId", order.getUserId());
            orderEvent.put("totalAmount", order.getTotalAmount());
            orderEvent.put("status", order.getStatus());
            orderEvent.put("address", order.getAddress());
            orderEvent.put("timestamp", LocalDateTime.now());

            String orderPayload = objectMapper.writeValueAsString(orderEvent);
            kafkaTemplate.send(ORDER_TOPIC, order.getId(), orderPayload);

            // --------------------------
            // PRODUCT LEVEL EVENTS
            // --------------------------
            for (var item : order.getItems()) {
                Map<String, Object> productEvent = new HashMap<>();
                productEvent.put("eventType", eventType);              // ORDER_CREATED, ORDER_CANCELLED, etc.
                productEvent.put("orderId", order.getId());
                productEvent.put("productId", item.getProductId());
                productEvent.put("quantity", item.getQuantity());
                productEvent.put("unitPrice", item.getPrice());        // price per item
                productEvent.put("totalPrice", item.getQuantity() * item.getPrice()); // total for this item
                productEvent.put("sellerId", item.getSellerId());     // seller for this product
                productEvent.put("userId", order.getUserId());
                productEvent.put("address", order.getAddress());
                productEvent.put("timestamp", LocalDateTime.now());

                String productPayload = objectMapper.writeValueAsString(productEvent);
                kafkaTemplate.send(PRODUCT_TOPIC, item.getProductId(), productPayload);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to publish order event", e);
        }
    }
}