package com.order_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private String id; // renamed from cartId → matches getId()

    // Owner of the cart
    private String userId;

    @Valid
    private List<CartItem> items = new ArrayList<>();

    private LocalDateTime updatedAt;
}