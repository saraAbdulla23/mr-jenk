package com.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CartResponse {

    private List<CartItemResponse> items;
    private double totalAmount;
}