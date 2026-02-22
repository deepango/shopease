package com.shopease.user.service;

import com.shopease.user.dto.UserResponse;
import com.shopease.user.entity.User;
import com.shopease.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUserById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(UUID id, String name) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        user.setName(name);
        return UserResponse.from(userRepository.save(user));
    }
}
