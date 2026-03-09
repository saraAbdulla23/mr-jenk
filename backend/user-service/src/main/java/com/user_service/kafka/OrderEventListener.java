package com.user_service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.user_service.model.User;
import com.user_service.model.User.OrderSummary;
import com.user_service.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class OrderEventListener {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrderEventListener(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // -------------------------------
    // Order-level events (user total spent, orders)
    // -------------------------------
    @KafkaListener(topics = "order-events", groupId = "user-service")
    public void onOrderEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);
            String eventType = (String) payload.get("eventType");

            String userId = (String) payload.get("userId");
            String orderId = (String) payload.get("orderId");
            String status = (String) payload.get("status");
            Double totalAmountDouble = payload.get("totalAmount") != null
                    ? ((Number) payload.get("totalAmount")).doubleValue()
                    : 0.0;

            if (userId == null || orderId == null || status == null) {
                System.err.println("❌ Invalid order payload: " + payload);
                return;
            }

            userRepository.findById(userId).ifPresent(user -> {
                // ----------------------
                // Update user totalSpent
                // ----------------------
                if ("ORDER_CREATED".equals(eventType)) {
                    user.setTotalSpent(user.getTotalSpent().add(BigDecimal.valueOf(totalAmountDouble)));
                } else if ("ORDER_CANCELLED".equals(eventType)) {
                    user.setTotalSpent(user.getTotalSpent().subtract(BigDecimal.valueOf(totalAmountDouble)));
                }

                // ----------------------
                // Update orders summary
                // ----------------------
                boolean exists = user.getOrders().stream()
                        .anyMatch(o -> o.getOrderId().equals(orderId));
                if (!exists && "ORDER_CREATED".equals(eventType)) {
                    OrderSummary summary = new OrderSummary(orderId, status, BigDecimal.valueOf(totalAmountDouble));
                    user.getOrders().add(summary);
                }

                userRepository.save(user);
                System.out.println("✅ User " + userId + " updated with order " + orderId);
            });

        } catch (Exception e) {
            System.err.println("❌ Failed to process order event: " + e.getMessage());
        }
    }

    // -------------------------------
    // Product-level events (user most-bought, seller revenue)
    // -------------------------------
    @KafkaListener(topics = "product-events", groupId = "user-service")
    public void onProductEvent(String message) {
        try {
            Map<String, Object> payload = objectMapper.readValue(message, Map.class);

            String userId = (String) payload.get("userId");
            String sellerId = (String) payload.get("sellerId");
            String productId = (String) payload.get("productId");
            Integer quantity = payload.get("quantity") != null ? ((Number) payload.get("quantity")).intValue() : 0;
            Double totalPriceDouble = payload.get("totalPrice") != null ? ((Number) payload.get("totalPrice")).doubleValue() : 0.0;

            // ----------------------
            // Update user analytics
            // ----------------------
            if (userId != null) {
                userRepository.findById(userId).ifPresent(user -> {
                    Map<String, Integer> productCounts = user.getProductCounts();
                    productCounts.put(productId, productCounts.getOrDefault(productId, 0) + quantity);
                    userRepository.save(user);
                });
            }

            // ----------------------
            // Update seller analytics
            // ----------------------
            if (sellerId != null) {
                userRepository.findById(sellerId).ifPresent(seller -> {
                    // revenue
                    Map<String, BigDecimal> sellerRevenue = seller.getSellerRevenue();
                    sellerRevenue.put(sellerId, sellerRevenue.getOrDefault(sellerId, BigDecimal.ZERO)
                            .add(BigDecimal.valueOf(totalPriceDouble)));

                    // units sold per product
                    Map<String, Integer> productSales = seller.getProductSalesCounts();
                    productSales.put(productId, productSales.getOrDefault(productId, 0) + quantity);

                    userRepository.save(seller);
                });
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to process product event: " + e.getMessage());
        }
    }
}