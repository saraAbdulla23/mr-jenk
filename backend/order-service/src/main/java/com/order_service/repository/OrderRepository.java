package com.order_service.repository;

import com.order_service.model.Order;
import com.order_service.model.OrderStatus;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {

    Page<Order> findByUserId(String userId, Pageable pageable);

    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

    Page<Order> findByItemsSellerId(String sellerId, Pageable pageable);

    Page<Order> findByItemsSellerIdAndStatus(
            String sellerId,
            OrderStatus status,
            Pageable pageable
    );

    Page<Order> findByItemsSellerIdAndCreatedAtBetween(
            String sellerId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<Order> findByItemsSellerIdAndStatusAndCreatedAtBetween(
            String sellerId,
            OrderStatus status,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
}