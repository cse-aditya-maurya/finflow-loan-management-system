package com.finflow.auth.controller;

import com.finflow.auth.dto.*;
import com.finflow.auth.service.AuthService;
import com.finflow.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerUnitTest {

    @Mock
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthController authController;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        authResponse = new AuthResponse("token123", 1L, "Success");
    }

    @Test
    void signup_ShouldReturnAuthResponse() {
        when(authService.signup(any(SignupRequest.class))).thenReturn(authResponse);
        
        AuthResponse response = authController.signup(signupRequest);
        
        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("Success", response.getMessage());
        verify(authService, times(1)).signup(signupRequest);
    }

    @Test
    void login_ShouldReturnAuthResponse() {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);
        
        AuthResponse response = authController.login(loginRequest);
        
        assertNotNull(response);
        assertEquals("token123", response.getToken());
        assertEquals(1L, response.getUserId());
        verify(authService, times(1)).login(loginRequest);
    }

    @Test
    void acceptTerms_ShouldReturnSuccessMessage() {
        String expectedMessage = "Terms accepted";
        when(authService.acceptTerms(1L)).thenReturn(expectedMessage);
        
        String response = authController.acceptTerms(1L);
        
        assertEquals(expectedMessage, response);
        verify(authService, times(1)).acceptTerms(1L);
    }

    @Test
    void testToken_ShouldReturnUserDetails() {
        String authHeader = "Bearer test-token";
        when(jwtUtil.extractEmail("test-token")).thenReturn("test@example.com");
        when(jwtUtil.extractRole("test-token")).thenReturn("USER");
        
        String response = authController.test(authHeader);
        
        assertEquals("Email: test@example.com, Role: USER", response);
        verify(jwtUtil, times(1)).extractEmail("test-token");
        verify(jwtUtil, times(1)).extractRole("test-token");
    }

    @Test
    void getUserIdByEmail_ShouldReturnUserId() {
        when(authService.getUserIdByEmail("test@example.com")).thenReturn(1L);
        
        Long userId = authController.getUserIdByEmail("test@example.com");
        
        assertEquals(1L, userId);
        verify(authService, times(1)).getUserIdByEmail("test@example.com");
    }

    @Test
    void testToken_WithInvalidHeader_ShouldThrowException() {
        String authHeader = "InvalidHeader token";
        
        assertThrows(StringIndexOutOfBoundsException.class, () -> {
            authController.test(authHeader);
        });
        
        verify(jwtUtil, never()).extractEmail(any());
    }

    @Test
    void testToken_WithNullHeader_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> {
            authController.test(null);
        });
        
        verify(jwtUtil, never()).extractEmail(any());
    }
}