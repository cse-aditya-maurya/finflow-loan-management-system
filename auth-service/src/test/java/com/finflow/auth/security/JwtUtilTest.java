package com.finflow.auth.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKeyForJWTTokenGenerationAndValidation12345");
        jwtUtil.init();
    }

    @Test
    void generateToken_Success() {
        String token = jwtUtil.generateToken("test@example.com", "USER");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_ValidToken_Success() {
        String token = jwtUtil.generateToken("test@example.com", "USER");

        String email = jwtUtil.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void extractRole_ValidToken_Success() {
        String token = jwtUtil.generateToken("test@example.com", "ADMIN");

        String role = jwtUtil.extractRole(token);

        assertEquals("ADMIN", role);
    }

    @Test
    void getClaims_ValidToken_Success() {
        String token = jwtUtil.generateToken("test@example.com", "USER");

        Claims claims = jwtUtil.getClaims(token);

        assertNotNull(claims);
        assertEquals("test@example.com", claims.getSubject());
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken("test@example.com", "USER");

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void generateToken_DifferentRoles_Success() {
        String userToken = jwtUtil.generateToken("user@example.com", "USER");
        String adminToken = jwtUtil.generateToken("admin@example.com", "ADMIN");

        assertEquals("USER", jwtUtil.extractRole(userToken));
        assertEquals("ADMIN", jwtUtil.extractRole(adminToken));
    }
}
