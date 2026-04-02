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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final EmailService emailService;

    /** Register a new user (force ROLE_USER) */
    @Transactional
    public AuthResponse register(@Valid RegisterReq request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email is already in use.");
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.ROLE_USER); // Always force ROLE_USER for normal registration

        User savedUser = userRepository.save(newUser);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, savedUser, "SUCCESS");
    }

    /** Login → authenticate password and send OTP */
    @Transactional
    public AuthResponse login(@Valid AuthReq request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        String otp = otpService.generateOtp(user.getEmail());
        emailService.sendOtp(user.getEmail(), otp);

        return new AuthResponse(null, null, "OTP_REQUIRED");
    }

    /** Verify OTP → return JWT if correct */
    @Transactional
    public AuthResponse verifyOtp(String email, String otp) {
        otpService.verifyOtp(email, otp);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user, "SUCCESS");
    }

    /** Get currently authenticated user */
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found."));
    }
}