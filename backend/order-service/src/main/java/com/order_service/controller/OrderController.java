package com.order_service.controller;

import com.order_service.model.Order;
import com.order_service.model.OrderStatus;
import com.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // -------------------------
    // CLIENT → GET MY ORDERS
    // -------------------------
    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping
    public Page<Order> getMyOrders(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String userId = jwt.getClaimAsString("userId");
        return orderService.getUserOrders(userId, status, page, size);
    }

    // -------------------------
    // SELLER → GET ORDERS
    // -------------------------
    @PreAuthorize("hasRole('SELLER')")
@GetMapping("/seller")
public Page<Order> getSellerOrders(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) OrderStatus status,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) {

    String sellerId = jwt.getClaimAsString("userId");

    LocalDateTime start = startDate != null
        ? LocalDate.parse(startDate).atStartOfDay()
        : null;
        LocalDateTime end = endDate != null
        ? LocalDate.parse(endDate).atTime(23,59,59)
        : null;

    return orderService.getSellerOrders(sellerId, status, start, end, page, size);
}

    // -------------------------
    // CLIENT → CANCEL ORDER
    // -------------------------
    @PreAuthorize("hasRole('CLIENT')")
    @PutMapping("/{id}/cancel")
    public Order cancelOrder(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getClaimAsString("userId");
        return orderService.cancelOrder(id, userId);
    }

    // -------------------------
    // CLIENT → REDO ORDER
    // -------------------------
    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/redo")
    public Order redoOrder(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String userId = jwt.getClaimAsString("userId");
        return orderService.redoOrder(id, userId);
    }

    // -------------------------
    // SELLER → MARK AS DELIVERED
    // -------------------------
    @PreAuthorize("hasRole('SELLER')")
    @PutMapping("/{id}/deliver")
    public Order markAsDelivered(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sellerId = jwt.getClaimAsString("userId");
        return orderService.markAsDelivered(id, sellerId);
    }

    // Seller must be able to open a single order.
    @PreAuthorize("hasRole('SELLER')")
@GetMapping("/{id}")
public Order getOrderDetails(
        @PathVariable String id,
        @AuthenticationPrincipal Jwt jwt
) {

    String sellerId = jwt.getClaimAsString("userId");
    return orderService.getSellerOrderDetails(id, sellerId);
}
}