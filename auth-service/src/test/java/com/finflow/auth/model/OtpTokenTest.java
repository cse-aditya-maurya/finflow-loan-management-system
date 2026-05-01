package com.finflow.auth.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OtpTokenTest {

    @Test
    void createOtpToken_AllFields_Success() {
        LocalDateTime now = LocalDateTime.now();
        OtpToken token = new OtpToken();
        token.setId(1L);
        token.setEmail("test@example.com");
        token.setOtp("123456");
        token.setExpiresAt(now.plusMinutes(5));
        token.setAttempts(0);
        token.setUsedAt(null);
        token.setCreatedAt(now);

        assertEquals(1L, token.getId());
        assertEquals("test@example.com", token.getEmail());
        assertEquals("123456", token.getOtp());
        assertEquals(0, token.getAttempts());
        assertNull(token.getUsedAt());
        assertNotNull(token.getCreatedAt());
    }

    @Test
    void incrementAttempts_Success() {
        OtpToken token = new OtpToken();
        token.setAttempts(0);

        token.setAttempts(token.getAttempts() + 1);

        assertEquals(1, token.getAttempts());
    }

    @Test
    void markAsUsed_Success() {
        OtpToken token = new OtpToken();
        LocalDateTime now = LocalDateTime.now();
        token.setUsedAt(now);

        assertNotNull(token.getUsedAt());
    }
}
