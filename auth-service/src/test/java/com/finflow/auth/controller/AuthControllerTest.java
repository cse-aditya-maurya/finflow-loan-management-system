package com.finflow.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.auth.dto.*;
import com.finflow.auth.model.Role;
import com.finflow.auth.security.JwtUtil;
import com.finflow.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtUtil jwtUtil;

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

        authResponse = new AuthResponse("jwt-token", 1L, "Success");
    }

    @Test
    void signup_ValidRequest_ReturnsSuccess() throws Exception {
        when(authService.signup(any(SignupRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void login_ValidCredentials_ReturnsToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void verifyOtp_ValidRequest_ReturnsSuccess() throws Exception {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");

        when(authService.verifyOtp(any(VerifyOtpRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/verify-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void resendOtp_ValidRequest_ReturnsSuccess() throws Exception {
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("test@example.com");

        when(authService.resendOtp(any(ResendOtpRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/resend-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void acceptTerms_ValidUserId_ReturnsSuccess() throws Exception {
        when(authService.acceptTerms(anyLong())).thenReturn("Terms accepted successfully");

        mockMvc.perform(post("/auth/accept-terms/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Terms accepted successfully"));
    }

    @Test
    void testToken_ValidToken_ReturnsUserDetails() throws Exception {
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(jwtUtil.extractRole(anyString())).thenReturn("USER");

        mockMvc.perform(get("/auth/test-token")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email: test@example.com, Role: USER"));
    }

    @Test
    void getUserIdByEmail_ValidEmail_ReturnsUserId() throws Exception {
        when(authService.getUserIdByEmail(anyString())).thenReturn(1L);

        mockMvc.perform(get("/auth/user/email/test@example.com")
                        .header("Authorization", "Bearer jwt-token"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void getUserProfile_ValidUserId_ReturnsProfile() throws Exception {
        UserProfileResponse profile = new UserProfileResponse("test@example.com", "Test User");
        when(authService.getUserProfile(anyLong())).thenReturn(profile);

        mockMvc.perform(get("/auth/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"));
    }

    @Test
    void forgotPassword_ValidRequest_ReturnsSuccess() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }

    @Test
    void resetPassword_ValidRequest_ReturnsSuccess() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setNewPassword("newPassword123");

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"));
    }
}
