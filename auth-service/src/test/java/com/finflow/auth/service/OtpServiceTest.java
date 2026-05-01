package com.finflow.auth.service;

import com.finflow.auth.exception.AuthException;
import com.finflow.auth.model.OtpToken;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.OtpTokenRepository;
import com.finflow.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private OtpTokenRepository otpTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OtpService otpService;

    private User testUser;
    private OtpToken otpToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "expiryMinutes", 5L);
        ReflectionTestUtils.setField(otpService, "maxAttempts", 3);

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser.setTermsAccepted(false);

        otpToken = new OtpToken();
        otpToken.setId(1L);
        otpToken.setEmail("test@example.com");
        otpToken.setOtp("123456");
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpToken.setAttempts(0);
        otpToken.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void sendOtp_Success() {
        doNothing().when(otpTokenRepository).deleteAllByEmailAndUsedAtIsNull(anyString());
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        String otp = otpService.sendOtp(testUser);

        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(otpTokenRepository).deleteAllByEmailAndUsedAtIsNull(testUser.getEmail());
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void verifyOtpAndActivate_ValidOtp_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(anyString()))
                .thenReturn(Optional.of(otpToken));
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(otpTokenRepository).deleteAllByEmailAndUsedAtIsNull(anyString());

        User result = otpService.verifyOtpAndActivate("test@example.com", "123456");

        assertNotNull(result);
        assertTrue(result.isTermsAccepted());
        verify(userRepository).save(testUser);
    }

    @Test
    void verifyOtpAndActivate_InvalidOtp_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(anyString()))
                .thenReturn(Optional.of(otpToken));
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);

        AuthException exception = assertThrows(AuthException.class,
                () -> otpService.verifyOtpAndActivate("test@example.com", "999999"));

        assertTrue(exception.getMessage().contains("Invalid OTP"));
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void verifyOtpAndActivate_ExpiredOtp_ThrowsException() {
        otpToken.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(anyString()))
                .thenReturn(Optional.of(otpToken));
        doNothing().when(otpTokenRepository).deleteAllByEmailAndUsedAtIsNull(anyString());

        AuthException exception = assertThrows(AuthException.class,
                () -> otpService.verifyOtpAndActivate("test@example.com", "123456"));

        assertTrue(exception.getMessage().contains("OTP expired"));
        verify(otpTokenRepository).deleteAllByEmailAndUsedAtIsNull(anyString());
    }

    @Test
    void verifyOtpAndActivate_NoOtpFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(anyString()))
                .thenReturn(Optional.empty());

        AuthException exception = assertThrows(AuthException.class,
                () -> otpService.verifyOtpAndActivate("test@example.com", "123456"));

        assertTrue(exception.getMessage().contains("OTP expired or invalid"));
    }

    @Test
    void verifyOtpAndActivate_MaxAttemptsExceeded_ThrowsException() {
        otpToken.setAttempts(3);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(otpTokenRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(anyString()))
                .thenReturn(Optional.of(otpToken));

        AuthException exception = assertThrows(AuthException.class,
                () -> otpService.verifyOtpAndActivate("test@example.com", "123456"));

        assertTrue(exception.getMessage().contains("Too many invalid attempts"));
    }

    @Test
    void verifyOtpAndActivate_UserNotFound_ThrowsException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(AuthException.class,
                () -> otpService.verifyOtpAndActivate("notfound@example.com", "123456"));
    }

    @Test
    void resendOtp_UnverifiedUser_Success() {
        doNothing().when(otpTokenRepository).deleteAllByEmailAndUsedAtIsNull(anyString());
        when(otpTokenRepository.save(any(OtpToken.class))).thenReturn(otpToken);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        otpService.resendOtp(testUser);

        verify(otpTokenRepository).deleteAllByEmailAndUsedAtIsNull(testUser.getEmail());
        verify(otpTokenRepository).save(any(OtpToken.class));
    }

    @Test
    void resendOtp_VerifiedUser_NoAction() {
        testUser.setTermsAccepted(true);

        otpService.resendOtp(testUser);

        verify(otpTokenRepository, never()).save(any(OtpToken.class));
    }
}
