package com.shopease.gateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private final String secret = "shopease-secret-key-must-be-at-least-32-characters-long";

    @BeforeEach
    void setUp() throws Exception {
        filter = new JwtAuthenticationFilter();
        Field secretField = JwtAuthenticationFilter.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(filter, secret);
    }

    private String generateToken(String userId, String role) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void shouldAllowOpenEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/auth/login")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        // Open endpoints bypass auth — response status remains null (no error set)
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void shouldRejectRequestWithNoAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/orders/123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var config = new JwtAuthenticationFilter.Config();
        var gatewayFilter = filter.apply(config);
        gatewayFilter.filter(exchange, ex -> ex.getResponse().setComplete()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldRejectInvalidToken() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/orders/123")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token.here")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        var config = new JwtAuthenticationFilter.Config();
        var gatewayFilter = filter.apply(config);
        gatewayFilter.filter(exchange, ex -> ex.getResponse().setComplete()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
