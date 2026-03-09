package com.user_service.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private Role role;

    private String avatar;
    private String address;

    // Store orders with summary info
    private List<OrderSummary> orders = new ArrayList<>();

    // -------------------------------
    // Analytics fields
    // -------------------------------
    private BigDecimal totalSpent = BigDecimal.ZERO;                     // for clients
    private Map<String, Integer> productCounts = new HashMap<>();        // user: productId → quantity bought

    // Seller-specific analytics
    private Map<String, BigDecimal> sellerRevenue = new HashMap<>();     // seller: sellerId → revenue
    private Map<String, Integer> productSalesCounts = new HashMap<>();  // seller: productId → units sold

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private String orderId;
        private String status;
        private BigDecimal totalAmount;
    }
}