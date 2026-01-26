package com.user_service.dto;

import com.user_service.model.Role;
import com.user_service.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserReq {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password; // optional

    private String avatar; // optional (SELLER only)

    private Role role; // optional, service layer enforces role

    /**
     * Converts DTO to User object for service layer.
     * Only sets fields that can be updated.
     */
    public User toUser() {
        User user = new User();
        user.setName(this.name);
        user.setEmail(this.email);
        if (this.password != null && !this.password.isBlank()) {
            user.setPassword(this.password);
        }
        user.setAvatar(this.avatar);
        user.setRole(this.role); // service layer ensures role doesn't change if needed
        return user;
    }
}
