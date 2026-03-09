package com.order_service.controller;

import com.order_service.dto.CartResponse;
import com.order_service.model.CartItem;
import com.order_service.model.Order;
import com.order_service.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // -------------------------
    // GET CART
    // -------------------------
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal Jwt jwt) {

        String userId = jwt.getClaimAsString("userId");
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    // -------------------------
    // ADD ITEM
    // -------------------------
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CartItem item) {

        String userId = jwt.getClaimAsString("userId");
        return ResponseEntity.ok(cartService.addItem(userId, item));
    }

    // -------------------------
    // UPDATE QUANTITY
    // -------------------------
    @PutMapping("/update")
    public ResponseEntity<CartResponse> updateItem(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String productId,
            @RequestParam int quantity) {

        String userId = jwt.getClaimAsString("userId");
        return ResponseEntity.ok(cartService.updateItem(userId, productId, quantity));
    }

    // -------------------------
    // REMOVE ITEM
    // -------------------------
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String productId) {

        String userId = jwt.getClaimAsString("userId");
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    // -------------------------
    // CHECKOUT
    // -------------------------
    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam String address) {

        String userId = jwt.getClaimAsString("userId");
        return ResponseEntity.ok(cartService.checkout(userId, address));
    }
}