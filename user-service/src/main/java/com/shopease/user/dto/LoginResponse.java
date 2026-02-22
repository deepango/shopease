package com.shopease.user.dto;

public class LoginResponse {
    private String token;
    private String tokenType = "Bearer";
    private long expiresIn;
    private String userId;
    private String email;
    private String role;

    public LoginResponse(String token, long expiresIn, String userId, String email, String role) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.userId = userId;
        this.email = email;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getTokenType() { return tokenType; }
    public long getExpiresIn() { return expiresIn; }
    public String getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
}
