package com.order_service.util;

import com.order_service.exception.BadRequestException;
import com.order_service.model.Order;
import com.order_service.model.OrderStatus;

public class OrderStatusValidator {

    public static void validateTransition(Order order, OrderStatus newStatus) {

        OrderStatus current = order.getStatus();

        if (current == OrderStatus.DELIVERED) {
            throw new BadRequestException("Delivered orders cannot be modified");
        }

        if (current == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled orders cannot be modified");
        }

        if (current == OrderStatus.CREATED &&
                (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.DELIVERED)) {
            return;
        }

        throw new BadRequestException("Invalid status transition");
    }
}