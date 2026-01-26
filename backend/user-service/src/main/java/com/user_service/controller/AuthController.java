package com.user_service.controller;

import com.user_service.dto.AuthReq;
import com.user_service.dto.AuthResponse;
import com.user_service.dto.RegisterReq;
import com.user_service.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Handles login and registration endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthService authService;

    @Autowired
    public void injectAuthService(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthReq request) {
    System.out.println("Received login request: " + request.getEmail());
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
}


    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterReq request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
}
