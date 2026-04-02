package com.user_service.controller;

import com.user_service.dto.AuthReq;
import com.user_service.dto.AuthResponse;
import com.user_service.dto.RegisterReq;
import com.user_service.dto.OtpVerifyReq;
import com.user_service.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** Login → sends OTP */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthReq request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /** Verify OTP → returns JWT if OTP is correct */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyReq request) {
        AuthResponse response = authService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    /** Register a new user */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterReq request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
}