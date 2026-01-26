package com.user_service.service;

import com.user_service.dto.UpdateUserReq;
import com.user_service.exception.ResourceAlreadyExistsException;
import com.user_service.exception.ResourceNotFoundException;
import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Save a new user (used for registration).
     */
    public User save(User user) {
        userRepository.findByEmail(user.getEmail()).ifPresent(existing -> {
            throw new ResourceAlreadyExistsException("A user with this email already exists.");
        });

        if (!user.getPassword().startsWith("$2a$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("User role must be specified.");
        }

        return userRepository.save(user);
    }

    /**
     * Update the current user's own profile (SAFE VERSION).
     */
    public User updateProfile(UpdateUserReq request, User currentUser) {

        User existing = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // Email uniqueness check (exclude self)
        if (request.getEmail() != null &&
            !request.getEmail().equals(existing.getEmail()) &&
            userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("Email is already in use.");
        }

        existing.setName(request.getName());
        existing.setEmail(request.getEmail());

        // Optional password update
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Role CANNOT be changed
        existing.setRole(currentUser.getRole());

        // Avatar only for SELLER
        if (existing.getRole() == Role.ROLE_SELLER) {
            existing.setAvatar(request.getAvatar());
        }

        return userRepository.save(existing);
    }

    /**
     * Delete the current user's own account.
     */
    public void deleteById(String id, User currentUser) {
        if (!id.equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied: you can only delete your own account.");
        }

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
    }

    /**
     * Find a user by email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Find a user by ID.
     */
    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }
}
