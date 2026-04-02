package com.user_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Node("Otp")
public class Otp {

    @Id
    private String email;

    private String code;

    private LocalDateTime expiry;
}