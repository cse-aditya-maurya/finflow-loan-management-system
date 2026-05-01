package com.finflow.auth.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void createUser_AllFields_Success() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        user.setTermsAccepted(true);

        assertEquals(1L, user.getId());
        assertEquals("Test User", user.getName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("password", user.getPassword());
        assertEquals(Role.USER, user.getRole());
        assertTrue(user.isTermsAccepted());
    }

    @Test
    void createUser_DefaultTermsAccepted_IsFalse() {
        User user = new User();

        assertFalse(user.isTermsAccepted());
    }

    @Test
    void setRole_AdminRole_Success() {
        User user = new User();
        user.setRole(Role.ADMIN);

        assertEquals(Role.ADMIN, user.getRole());
    }
}
