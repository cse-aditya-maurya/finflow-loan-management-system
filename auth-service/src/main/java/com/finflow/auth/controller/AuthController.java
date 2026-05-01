package com.finflow.auth.controller;

import com.finflow.auth.dto.*;
import com.finflow.auth.service.AuthService;
import com.finflow.auth.security.JwtUtil;
import com.finflow.auth.model.Role;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "APIs for authentication and user management")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // ===============================
    // ✅ SIGNUP
    // ===============================
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    // ===============================
    // 🔐 VERIFY OTP
    // ===============================
    @Operation(summary = "Verify OTP and activate account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired OTP")
    })
    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    // ===============================
    // 🔁 RESEND OTP
    // ===============================
    @Operation(summary = "Resend OTP for account verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP resent successfully"),
            @ApiResponse(responseCode = "401", description = "User not found or already verified")
    })
    @PostMapping("/resend-otp")
    public AuthResponse resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        return authService.resendOtp(request);
    }

    // ===============================
    // ✅ LOGIN
    // ===============================
    @Operation(summary = "Login user and get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // ===============================
    // ✅ ACCEPT TERMS
    // ===============================
    @Operation(summary = "Accept terms and conditions")
    @PostMapping("/accept-terms/{userId}")
    public String acceptTerms(@PathVariable Long userId) {
        return authService.acceptTerms(userId);
    }

    // ===============================
    // 🔐 TEST JWT TOKEN
    // ===============================
    @Operation(summary = "Validate JWT token and extract details")
    @SecurityRequirement(name = "bearerAuth")   // 🔥 IMPORTANT
    @GetMapping("/test-token")
    public String test(@RequestHeader("Authorization") String header) {

        String token = header.substring(7);

        String email = jwtUtil.extractEmail(token);
        Role role = Role.valueOf(jwtUtil.extractRole(token));

        return "Email: " + email + ", Role: " + role;
    }

    // ===============================
    // 🔍 GET USER ID BY EMAIL
    // ===============================
    @Operation(summary = "Get user ID using email")
    @SecurityRequirement(name = "bearerAuth")   // 🔥 SECURED
    @GetMapping("/user/email/{email}")
    public Long getUserIdByEmail(@PathVariable String email) {
        return authService.getUserIdByEmail(email);
    }

    // ===============================
    // 👤 GET USER PROFILE BY ID
    // ===============================
    @Operation(summary = "Get email and name using userId")
    @GetMapping("/user/{userId}")
    public UserProfileResponse getUserProfile(@PathVariable Long userId) {
        return authService.getUserProfile(userId);
    }

    // ===============================
    // 👥 GET ALL USERS (ADMIN ONLY)
    // ===============================
    @Operation(summary = "Get all users (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin/all-users")
    public java.util.List<com.finflow.auth.model.User> getAllUsers(
            @RequestHeader("Authorization") String header) {
        
        String token = header.substring(7);
        Role role = Role.valueOf(jwtUtil.extractRole(token));
        
        if (role != Role.ADMIN) {
            throw new RuntimeException("Access Denied: Admin only");
        }
        
        return authService.getAllUsers();
    }

    // ===============================
    // 👤 GET USER BY ID (ADMIN ONLY)
    // ===============================
    @Operation(summary = "Get user by ID (Admin only)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/admin/user/{userId}")
    public com.finflow.auth.model.User getUserByIdAdmin(
            @RequestHeader("Authorization") String header,
            @PathVariable Long userId) {
        
        String token = header.substring(7);
        Role role = Role.valueOf(jwtUtil.extractRole(token));
        
        if (role != Role.ADMIN) {
            throw new RuntimeException("Access Denied: Admin only");
        }
        
        return authService.getUserByIdFull(userId);
    }

    // ===============================
    // 🔐 FORGOT PASSWORD
    // ===============================
    @Operation(summary = "Send OTP for password reset")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/forgot-password")
    public AuthResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return authService.forgotPassword(request);
    }

    // ===============================
    // 🔐 RESET PASSWORD
    // ===============================
    @Operation(summary = "Reset password after OTP verification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password reset successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/reset-password")
    public AuthResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}