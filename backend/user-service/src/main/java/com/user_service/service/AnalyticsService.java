package com.user_service.service;

import com.user_service.dto.ProductCount;
import com.user_service.model.User;
import com.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final UserRepository userRepository;

    // =========================
    // USER DASHBOARD
    // =========================
    public Map<String, Object> getUserDashboard(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> dashboard = new HashMap<>();

        // Total spent
        dashboard.put("totalSpent",
                user.getTotalSpent() != null ? user.getTotalSpent() : BigDecimal.ZERO);

        // Ensure productCounts map exists
        Map<String, Integer> productCounts =
                user.getProductCounts() != null ? user.getProductCounts() : new HashMap<>();

        // Top 5 most bought products
        List<ProductCount> topProducts = productCounts.entrySet()
        .stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(5)
        .map(e -> new ProductCount(e.getKey(), e.getValue()))
        .collect(Collectors.toList());

        dashboard.put("mostBoughtProducts", topProducts);

        // Placeholder for categories (future product-service integration)
        dashboard.put("topCategories", new HashMap<>());

        return dashboard;
    }

    // =========================
    // SELLER DASHBOARD
    // =========================
    public Map<String, Object> getSellerDashboard(String sellerId) {

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        Map<String, Object> dashboard = new HashMap<>();

        // Revenue map safety
        Map<String, BigDecimal> sellerRevenue =
                seller.getSellerRevenue() != null ? seller.getSellerRevenue() : new HashMap<>();

        BigDecimal totalRevenue = sellerRevenue.getOrDefault(sellerId, BigDecimal.ZERO);

        dashboard.put("totalRevenue", totalRevenue);

        // Sales counts safety
        Map<String, Integer> productSalesCounts =
                seller.getProductSalesCounts() != null ? seller.getProductSalesCounts() : new HashMap<>();

        // Best selling products
        List<ProductCount> topProducts = productSalesCounts.entrySet()
        .stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
        .limit(5)
        .map(e -> new ProductCount(e.getKey(), e.getValue()))
        .collect(Collectors.toList());

dashboard.put("bestSellingProducts", topProducts);

        dashboard.put("bestSellingProducts", topProducts);

        // Total units sold
        int totalUnitsSold = productSalesCounts.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        dashboard.put("unitsSold", totalUnitsSold);

        return dashboard;
    }
}