package com.user_service.controller;

import com.user_service.model.Role;
import com.user_service.model.User;
import com.user_service.service.AuthService;
import com.user_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    /** Get all users (Admin only) */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /** Get current logged-in user */
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    /** Update current user */
    @PutMapping("/me")
    public ResponseEntity<User> updateCurrentUser(@RequestBody User updatedUser) {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), updatedUser));
    }

    /** Delete current user */
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteCurrentUser() {
        User currentUser = authService.getCurrentUser();
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok("User deleted successfully");
    }

    // ===================== Admin Endpoints =====================

    /** Admin creates a new user */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> createUser(@Valid @RequestBody User newUser) {
        return ResponseEntity.ok(userService.createUser(newUser));
    }

    /** Admin updates any user */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    /** Admin deletes any user */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

    /** Admin gets a single user */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}