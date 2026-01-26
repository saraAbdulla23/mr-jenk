package com.user_service.service;

import com.user_service.dto.AuthReq;
import com.user_service.dto.AuthResponse;
import com.user_service.dto.RegisterReq;
import com.user_service.exception.ResourceAlreadyExistsException;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.repository.UserRepository;
import com.user_service.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final KafkaProducerService kafkaProducerService;

    /**
     * Registers a new user with ROLE_CLIENT or ROLE_SELLER and returns a JWT + User info.
     */
    public AuthResponse register(@Valid RegisterReq request) {
    // ✅ Throw custom exception instead of IllegalArgumentException
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
        throw new ResourceAlreadyExistsException("Email is already in use.");
    }

    Role assignedRole;
    try {
        assignedRole = Role.valueOf("ROLE_" + request.getRole().toUpperCase());
    } catch (Exception e) {
        throw new IllegalArgumentException("Invalid role. Must be CLIENT or SELLER.");
    }

    User newUser = new User();
    newUser.setName(request.getName());
    newUser.setEmail(request.getEmail());
    newUser.setPassword(passwordEncoder.encode(request.getPassword()));
    newUser.setRole(assignedRole);

    if (assignedRole == Role.ROLE_SELLER) {
        newUser.setAvatar(request.getAvatar());
    } else {
        newUser.setAvatar(null);
    }

    // Save user
    User savedUser = userRepository.save(newUser);

    // Generate JWT
    String token = jwtService.generateToken(savedUser);

    // Send Kafka event
    kafkaProducerService.sendUserEvent("USER_REGISTERED", savedUser);

    // Return AuthResponse with token + full user info
    return new AuthResponse(token, savedUser);
}

    /**
     * Authenticates an existing user and returns a JWT + User info.
     */
    public AuthResponse login(@Valid AuthReq request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        // Generate JWT
        String token = jwtService.generateToken(user);

        // Send Kafka event
        kafkaProducerService.sendUserEvent("USER_LOGGED_IN", user);

        // Return AuthResponse with token + full user info
        return new AuthResponse(token, user);
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));
    }
}
