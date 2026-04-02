package com.user_service.dto;

import com.user_service.model.User;

public class AuthResponse {
    private String token;
    private User user;
    private String status; // SUCCESS or OTP_REQUIRED

    public AuthResponse(String token, User user, String status) {
        this.token = token;
        this.user = user;
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public String getStatus() {
        return status;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}