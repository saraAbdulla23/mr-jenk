package com.product_service.kafka;

import lombok.Data;

@Data
public class UserRoleEvent {
    private String userId;
    private String role;
}
