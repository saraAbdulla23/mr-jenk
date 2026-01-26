package com.user_service.controller;

import com.user_service.dto.UpdateUserReq;
import com.user_service.model.User;
import com.user_service.service.AuthService;
import com.user_service.service.UserService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    // Get current user profile
    @GetMapping
    public ResponseEntity<User> getCurrentUser() {
        User currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    // Update current user profile
    @PutMapping
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UpdateUserReq request) {
        User currentUser = authService.getCurrentUser();
        // ✅ Use the correct service method
        User updatedUser = userService.updateProfile(request, currentUser);
        return ResponseEntity.ok(updatedUser);
    }

    // Delete current user
    @DeleteMapping
    public ResponseEntity<String> deleteAccount() {
        User currentUser = authService.getCurrentUser();
        userService.deleteById(currentUser.getId(), currentUser);
        return ResponseEntity.ok("Your account has been deleted.");
    }
}
