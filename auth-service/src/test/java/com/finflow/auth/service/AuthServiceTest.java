package com.finflow.auth.service;

import com.finflow.auth.dto.*;
import com.finflow.auth.exception.AuthException;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setTermsAccepted(true);

        signupRequest = new SignupRequest();
        signupRequest.setName("Test User");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void signup_NewUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(otpService.sendOtp(any(User.class))).thenReturn("123456");

        AuthResponse response = authService.signup(signupRequest);

        assertNotNull(response);
        assertEquals("OTP sent to your email. Please verify OTP.", response.getMessage());
        verify(userRepository).save(any(User.class));
        verify(otpService).sendOtp(any(User.class));
    }

    @Test
    void signup_ExistingUserNotVerified_ResendOtp() {
        testUser.setTermsAccepted(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(otpService).resendOtp(any(User.class));

        AuthResponse response = authService.signup(signupRequest);

        assertNotNull(response);
        assertEquals("OTP resent successfully. Please verify OTP.", response.getMessage());
        verify(otpService).resendOtp(any(User.class));
    }

    @Test
    void signup_ExistingUserVerified_ReturnMessage() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.signup(signupRequest);

        assertNotNull(response);
        assertEquals("User already verified, you can directly login", response.getMessage());
        verify(otpService, never()).sendOtp(any(User.class));
    }

    @Test
    void login_ValidCredentials_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("Login successful", response.getMessage());
        verify(jwtUtil).generateToken(testUser.getEmail(), testUser.getRole().name());
    }

    @Test
    void login_InvalidEmail_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_InvalidPassword_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.login(loginRequest));
    }

    @Test
    void login_EmailNotVerified_ThrowsException() {
        testUser.setTermsAccepted(false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AuthException exception = assertThrows(AuthException.class, () -> authService.login(loginRequest));
        assertTrue(exception.getMessage().contains("Email not verified"));
    }

    @Test
    void verifyOtp_ValidOtp_Success() {
        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("test@example.com");
        request.setOtp("123456");

        when(otpService.verifyOtpAndActivate(anyString(), anyString())).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("jwt-token");

        AuthResponse response = authService.verifyOtp(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("OTP verified successfully", response.getMessage());
    }

    @Test
    void resendOtp_UnverifiedUser_Success() {
        testUser.setTermsAccepted(false);
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        doNothing().when(otpService).resendOtp(any(User.class));

        AuthResponse response = authService.resendOtp(request);

        assertNotNull(response);
        assertEquals("OTP resent successfully. Please verify OTP.", response.getMessage());
        verify(otpService).resendOtp(testUser);
    }

    @Test
    void resendOtp_VerifiedUser_ReturnMessage() {
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.resendOtp(request);

        assertNotNull(response);
        assertEquals("Email already verified. Please login.", response.getMessage());
        verify(otpService, never()).resendOtp(any(User.class));
    }

    @Test
    void resendOtp_UserNotFound_ThrowsException() {
        ResendOtpRequest request = new ResendOtpRequest();
        request.setEmail("notfound@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.resendOtp(request));
    }

    @Test
    void acceptTerms_ValidUser_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String result = authService.acceptTerms(1L);

        assertEquals("Terms accepted successfully", result);
        verify(userRepository).save(testUser);
    }

    @Test
    void acceptTerms_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.acceptTerms(1L));
    }

    @Test
    void getUserProfile_ValidUser_Success() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        UserProfileResponse response = authService.getUserProfile(1L);

        assertNotNull(response);
        assertEquals(testUser.getEmail(), response.getEmail());
        assertEquals(testUser.getName(), response.getName());
    }

    @Test
    void getUserProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getUserProfile(1L));
    }

    @Test
    void getUserIdByEmail_ValidEmail_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        Long userId = authService.getUserIdByEmail("test@example.com");

        assertEquals(testUser.getId(), userId);
    }

    @Test
    void getUserIdByEmail_EmailNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authService.getUserIdByEmail("notfound@example.com"));
    }

    @Test
    void forgotPassword_ValidUser_Success() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("test@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpService.sendOtp(any(User.class))).thenReturn("123456");

        AuthResponse response = authService.forgotPassword(request);

        assertNotNull(response);
        assertEquals("Password reset OTP sent to your email.", response.getMessage());
        verify(otpService).sendOtp(testUser);
    }

    @Test
    void forgotPassword_UserNotFound_ThrowsException() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("notfound@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.forgotPassword(request));
    }

    @Test
    void resetPassword_ValidUser_Success() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("test@example.com");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.resetPassword(request);

        assertNotNull(response);
        assertEquals("Password reset successfully. Please login with your new password.", response.getMessage());
        verify(userRepository).save(testUser);
    }

    @Test
    void resetPassword_UserNotFound_ThrowsException() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("notfound@example.com");
        request.setNewPassword("newPassword123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthException.class, () -> authService.resetPassword(request));
    }
}
