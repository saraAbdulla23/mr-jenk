package com.order_service.service;

import com.order_service.dto.CartItemResponse;
import com.order_service.dto.CartResponse;
import com.order_service.event.OrderEventPublisher;
import com.order_service.exception.BadRequestException;
import com.order_service.exception.ResourceNotFoundException;
import com.order_service.model.*;
import com.order_service.repository.CartRepository;
import com.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    // -------------------------
    // GET CART (AUTO CREATE)
    // -------------------------
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    newCart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(newCart);
                });

        return mapToCartResponse(cart);
    }

    // -------------------------
    // ADD ITEM TO CART
    // -------------------------
    public CartResponse addItem(String userId, CartItem newItem) {

        if (newItem.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().stream()
                .filter(item -> item.getProductId().equals(newItem.getProductId()))
                .findFirst()
                .ifPresentOrElse(
                        existingItem -> existingItem.setQuantity(existingItem.getQuantity() + newItem.getQuantity()),
                        () -> cart.getItems().add(newItem)
                );

        cart.setUpdatedAt(LocalDateTime.now());

        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    // -------------------------
    // UPDATE ITEM QUANTITY
    // -------------------------
    public CartResponse updateItem(String userId, String productId, int quantity) {

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        item.setQuantity(quantity);
        cart.setUpdatedAt(LocalDateTime.now());

        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    // -------------------------
    // REMOVE ITEM
    // -------------------------
    public CartResponse removeItem(String userId, String productId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        boolean removed = cart.getItems()
                .removeIf(i -> i.getProductId().equals(productId));

        if (!removed) {
            throw new ResourceNotFoundException("Item not found in cart");
        }

        cart.setUpdatedAt(LocalDateTime.now());

        Cart saved = cartRepository.save(cart);
        return mapToCartResponse(saved);
    }

    // -------------------------
    // CHECKOUT CART → CREATE ORDER
    // -------------------------
    @Transactional
    public Order checkout(String userId, String address) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot checkout empty cart");
        }

        double total = cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        Order order = new Order();
        order.setUserId(userId);
        order.setItems(
                cart.getItems().stream()
                        .map(item -> new OrderItem(
                                item.getProductId(),
                                item.getName(),
                                item.getPrice(),
                                item.getSellerId(),
                                item.getQuantity()
                        ))
                        .collect(Collectors.toList())
        );
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CREATED);
        order.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);
        order.setAddress(address);
        order.setCreatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        orderEventPublisher.publishEvent("ORDER_CREATED", savedOrder);

        cart.getItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);

        return savedOrder;
    }

    // -------------------------
    // PRIVATE MAPPER
    // -------------------------
    private CartResponse mapToCartResponse(Cart cart) {

        var itemResponses = cart.getItems().stream()
                .map(item -> {
                    double subtotal = item.getPrice() * item.getQuantity();
                    return new CartItemResponse(
                            item.getProductId(),
                            item.getName(),
                            item.getPrice(),
                            item.getQuantity(),
                            subtotal
                    );
                })
                .toList();

        double total = itemResponses.stream()
                .mapToDouble(CartItemResponse::getSubtotal)
                .sum();

        return new CartResponse(itemResponses, total);
    }
}