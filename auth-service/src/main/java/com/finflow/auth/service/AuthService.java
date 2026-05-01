package com.finflow.auth.service;

import com.finflow.auth.dto.*;
import com.finflow.auth.exception.AuthException;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.security.JwtUtil;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;

    //  SIGNUP
    public AuthResponse signup(SignupRequest request) {

        String email = normalizeEmail(request.getEmail());

        //  CHECK: USER ALREADY EXISTS
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // Keep signup idempotent: update provided profile/password, then resend OTP if not verified.
            user.setName(request.getName());
            user.setPassword(passwordEncoder.encode(request.getPassword()));

            if (user.isTermsAccepted()) {
                return new AuthResponse(
                        null,
                        user.getId(),
                        "User already verified, you can directly login"
                );
            }

            userRepository.save(user);
            otpService.resendOtp(user);
            return new AuthResponse(
                    null,
                    user.getId(),
                    "OTP resent successfully. Please verify OTP."
            );
        }

        // ✅ CREATE NEW USER (unverified until OTP validation)
        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setTermsAccepted(false);

        User savedUser = userRepository.save(user);

        // ✅ Generate OTP + send via notification-service (RabbitMQ)
        otpService.sendOtp(savedUser);

        return new AuthResponse(
                null,
                savedUser.getId(),
                "OTP sent to your email. Please verify OTP."
        );
    }

    // LOGIN
    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("Invalid email or password"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthException("Invalid email or password");
        }

        if (!user.isTermsAccepted()) {
            throw new AuthException("Email not verified. Please verify OTP first.");
        }

        // Generate JWT
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        
        System.out.println("===== AUTH SERVICE TOKEN =====");
        System.out.println(token);
        System.out.println("==============================");
        
        return new AuthResponse(token, null, "Login successful");
    }

    // 🔥 ACCEPT TERMS
    public String acceptTerms(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setTermsAccepted(true);

        userRepository.save(user);

        return "Terms accepted successfully";
    }

    // ===============================
    // 🔐 VERIFY OTP
    // ===============================
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());

        User activatedUser = otpService.verifyOtpAndActivate(email, request.getOtp());
        String token = jwtUtil.generateToken(
                activatedUser.getEmail(),
                activatedUser.getRole().name()
        );

        return new AuthResponse(token, activatedUser.getId(), "OTP verified successfully");
    }

    // ===============================
    // 🔁 RESEND OTP
    // ===============================
    public AuthResponse resendOtp(ResendOtpRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        if (user.isTermsAccepted()) {
            return new AuthResponse(null, user.getId(), "Email already verified. Please login.");
        }

        otpService.resendOtp(user);
        return new AuthResponse(null, user.getId(), "OTP resent successfully. Please verify OTP.");
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    // ===============================
    // 👤 PROFILE (for notifications)
    // ===============================
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserProfileResponse(user.getEmail(), user.getName());
    }
    
    public Long getUserIdByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getId();
    }

    // ===============================
    // 👥 GET ALL USERS (ADMIN)
    // ===============================
    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // ===============================
    // 👤 GET USER BY ID (FULL DETAILS)
    // ===============================
    public User getUserByIdFull(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ===============================
    // 🔐 FORGOT PASSWORD
    // ===============================
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        // Send OTP for password reset (works for verified users)
        otpService.sendOtp(user);
        
        return new AuthResponse(null, user.getId(), "Password reset OTP sent to your email.");
    }

    // ===============================
    // 🔐 RESET PASSWORD
    // ===============================
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new AuthResponse(null, user.getId(), "Password reset successfully. Please login with your new password.");
    }
}