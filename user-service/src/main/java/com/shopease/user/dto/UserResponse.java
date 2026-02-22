package com.shopease.user.dto;

import com.shopease.user.entity.User;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.name = user.getName();
        response.email = user.getEmail();
        response.role = user.getRole().name();
        response.createdAt = user.getCreatedAt();
        return response;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
