package com.shopease.user.controller;

import com.shopease.user.dto.UserResponse;
import com.shopease.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user profile")
    public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.updateUser(id, body.get("name")));
    }
}
