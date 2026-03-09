package com.order_service.service;

import com.order_service.event.OrderEventPublisher;
import com.order_service.exception.BadRequestException;
import com.order_service.exception.ResourceNotFoundException;
import com.order_service.model.*;
import com.order_service.repository.OrderRepository;
import com.order_service.util.OrderStatusValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderEventPublisher orderEventPublisher;

    // GET CLIENT ORDERS (OPTIONAL STATUS)
    public Page<Order> getUserOrders(String userId, OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (status != null) {
            return orderRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return orderRepository.findByUserId(userId, pageable);
    }

    // GET SELLER ORDERS
    public Page<Order> getSellerOrders(
        String sellerId,
        OrderStatus status,
        LocalDateTime startDate,
        LocalDateTime endDate,
        int page,
        int size
) {

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    if (status != null && startDate != null && endDate != null) {
        return orderRepository
                .findByItemsSellerIdAndStatusAndCreatedAtBetween(
                        sellerId,
                        status,
                        startDate,
                        endDate,
                        pageable
                );
    }

    if (status != null) {
        return orderRepository.findByItemsSellerIdAndStatus(sellerId, status, pageable);
    }

    if (startDate != null && endDate != null) {
        return orderRepository.findByItemsSellerIdAndCreatedAtBetween(
                sellerId,
                startDate,
                endDate,
                pageable
        );
    }

    return orderRepository.findByItemsSellerId(sellerId, pageable);
}
    // CANCEL ORDER (CLIENT ONLY)
    public Order cancelOrder(String orderId, String userId) {
        Order order = getOrderOrThrow(orderId);

        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("You do not own this order");
        }

        OrderStatusValidator.validateTransition(order, OrderStatus.CANCELLED);
        order.setStatus(OrderStatus.CANCELLED);

        Order saved = orderRepository.save(order);
        orderEventPublisher.publishEvent("ORDER_CANCELLED", saved);

        return saved;
    }

    // MARK AS DELIVERED (SELLER ONLY)
    public Order markAsDelivered(String orderId, String sellerId) {
        Order order = getOrderOrThrow(orderId);

        boolean containsSellerItem = order.getItems().stream()
                .anyMatch(item -> item.getSellerId().equals(sellerId));

        if (!containsSellerItem) {
            throw new BadRequestException("You are not authorized to deliver this order");
        }

        OrderStatusValidator.validateTransition(order, OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.DELIVERED);

        Order saved = orderRepository.save(order);
        orderEventPublisher.publishEvent("ORDER_DELIVERED", saved);

        return saved;
    }

    // REDO ORDER (CLIENT ONLY)
    public Order redoOrder(String orderId, String userId) {
        Order oldOrder = getOrderOrThrow(orderId);

        if (!oldOrder.getUserId().equals(userId)) {
            throw new BadRequestException("You do not own this order");
        }

        Order newOrder = new Order();
        newOrder.setUserId(userId);
        newOrder.setItems(oldOrder.getItems());
        newOrder.setTotalAmount(oldOrder.getTotalAmount());
        newOrder.setStatus(OrderStatus.CREATED);
        newOrder.setPaymentMethod(PaymentMethod.PAY_ON_DELIVERY);
        newOrder.setAddress(oldOrder.getAddress());
        newOrder.setCreatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(newOrder);
        orderEventPublisher.publishEvent("ORDER_REDO", saved);

        return saved;
    }

    // PRIVATE HELPER
    private Order getOrderOrThrow(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    // open one order
    public Order getSellerOrderDetails(String orderId, String sellerId) {

        Order order = getOrderOrThrow(orderId);
    
        boolean containsSellerItem = order.getItems().stream()
                .anyMatch(item -> item.getSellerId().equals(sellerId));
    
        if (!containsSellerItem) {
            throw new BadRequestException("You cannot view this order");
        }
    
        return order;
    }
}