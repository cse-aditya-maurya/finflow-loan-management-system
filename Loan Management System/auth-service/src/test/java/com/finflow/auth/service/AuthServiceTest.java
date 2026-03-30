package com.finflow.auth.service;

import com.finflow.auth.dto.*;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.security.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    // ✅ SIGNUP SUCCESS
    @Test
    void signup_success() {
        SignupRequest request = new SignupRequest();
        request.setName("Shivraj");
        request.setEmail("shivraj@gmail.com");
        request.setPassword("1234");

        when(userRepository.findByEmail("shivraj@gmail.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("1234"))
                .thenReturn("encoded123");

        AuthResponse response = authService.signup(request);

        assertEquals("User registered successfully", response.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ❌ SIGNUP EMAIL EXISTS
    @Test
    void signup_email_exists() {
        SignupRequest request = new SignupRequest();
        request.setEmail("shivraj@gmail.com");

        when(userRepository.findByEmail("shivraj@gmail.com"))
                .thenReturn(Optional.of(new User()));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.signup(request));

        assertEquals("Email already registered", ex.getMessage());
    }

    // ✅ LOGIN SUCCESS
    @Test
    void login_success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("shivraj@gmail.com");
        request.setPassword("1234");

        User user = new User();
        user.setEmail("shivraj@gmail.com");
        user.setPassword("encoded123");
        user.setRole(Role.USER);
        user.setTermsAccepted(true);

        when(userRepository.findByEmail("shivraj@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encoded123"))
                .thenReturn(true);

        when(jwtUtil.generateToken("shivraj@gmail.com", "USER"))
                .thenReturn("mocked-token");

        AuthResponse response = authService.login(request);

        assertEquals("Login successful", response.getMessage());
        assertEquals("mocked-token", response.getToken());
    }

    // ❌ LOGIN USER NOT FOUND
    @Test
    void login_user_not_found() {
        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("1234");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("User not found", ex.getMessage());
    }

    // ❌ WRONG PASSWORD
    @Test
    void login_wrong_password() {
        LoginRequest request = new LoginRequest();
        request.setEmail("shivraj@gmail.com");
        request.setPassword("wrong");

        User user = new User();
        user.setEmail("shivraj@gmail.com");
        user.setPassword("encoded123");
        user.setTermsAccepted(true);

        when(userRepository.findByEmail("shivraj@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("wrong", "encoded123"))
                .thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Invalid password", ex.getMessage());
    }

    // ❌ TERMS NOT ACCEPTED
    @Test
    void login_terms_not_accepted() {
        LoginRequest request = new LoginRequest();
        request.setEmail("shivraj@gmail.com");
        request.setPassword("1234");

        User user = new User();
        user.setEmail("shivraj@gmail.com");
        user.setPassword("encoded123");
        user.setTermsAccepted(false);

        when(userRepository.findByEmail("shivraj@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("1234", "encoded123"))
                .thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Please accept terms first", ex.getMessage());
    }

    // ✅ ACCEPT TERMS
    @Test
    void accept_terms_success() {
        User user = new User();
        user.setId(1L);
        user.setTermsAccepted(false);

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        String result = authService.acceptTerms(1L);

        assertEquals("Terms accepted successfully", result);
        assertTrue(user.isTermsAccepted());
    }

    // ❌ ACCEPT TERMS USER NOT FOUND
    @Test
    void accept_terms_user_not_found() {
        when(userRepository.findById(1L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.acceptTerms(1L));

        assertEquals("User not found", ex.getMessage());
    }

    // ✅ GET USER ID
    @Test
    void getUserId_success() {
        User user = new User();
        user.setId(10L);

        when(userRepository.findByEmail("shivraj@gmail.com"))
                .thenReturn(Optional.of(user));

        Long id = authService.getUserIdByEmail("shivraj@gmail.com");

        assertEquals(10L, id);
    }
}