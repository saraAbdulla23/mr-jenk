package com.order_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String id; // renamed from orderId → matches getId()

    // Buyer
    private String userId;

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    private List<OrderItem> items = new ArrayList<>();

    @NotNull(message = "Total amount is required")
    private Double totalAmount;

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotBlank(message = "Delivery address is required")
    private String address;

    private LocalDateTime createdAt;
}