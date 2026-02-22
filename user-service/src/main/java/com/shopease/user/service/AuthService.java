package com.shopease.user.service;

import com.shopease.user.dto.LoginRequest;
import com.shopease.user.dto.LoginResponse;
import com.shopease.user.dto.RegisterRequest;
import com.shopease.user.dto.UserResponse;
import com.shopease.user.entity.User;
import com.shopease.user.repository.UserRepository;
import com.shopease.user.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + request.getEmail());
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.Role.CUSTOMER);
        User saved = userRepository.save(user);
        return UserResponse.from(saved);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(
                user.getId().toString(),
                user.getEmail(),
                user.getRole().name()
        );
        return new LoginResponse(token, jwtUtil.getExpirationMs(), user.getId().toString(), user.getEmail(), user.getRole().name());
    }
}
