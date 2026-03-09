package com.user_service.controller;

import com.user_service.model.Role;
import com.user_service.service.AnalyticsService;
import com.user_service.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AnalyticsService analyticsService;
    private final AuthService authService;

    // =========================
    // USER DASHBOARD
    // =========================
    @GetMapping("/user")
    public ResponseEntity<?> getUserDashboard() {

        var currentUser = authService.getCurrentUser();

        var dashboard = analyticsService.getUserDashboard(currentUser.getId());

        return ResponseEntity.ok(dashboard);
    }

    // =========================
    // SELLER DASHBOARD
    // =========================
    @GetMapping("/seller")
    public ResponseEntity<?> getSellerDashboard() {

        var currentUser = authService.getCurrentUser();

        if (currentUser.getRole() != Role.ROLE_SELLER) {
            return ResponseEntity.status(403)
                    .body("Forbidden: Only sellers can access this dashboard");
        }

        var dashboard = analyticsService.getSellerDashboard(currentUser.getId());

        return ResponseEntity.ok(dashboard);
    }
}