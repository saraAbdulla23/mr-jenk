package com.user_service.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

@Data
public class OtpVerifyReq {
    @NotBlank
    private String email;

    @NotBlank
    private String otp;
}