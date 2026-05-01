package com.finflow.auth.integration;

import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.SignupRequest;
import com.finflow.auth.model.Role;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.UserRepository;
import com.finflow.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void signup_NewUser_CreatesUserInDatabase() {
        SignupRequest request = new SignupRequest();
        request.setName("Integration Test");
        request.setEmail("integration@test.com");
        request.setPassword("password123");

        authService.signup(request);

        assertTrue(userRepository.findByEmail("integration@test.com").isPresent());
    }

    @Test
    void login_VerifiedUser_ReturnsToken() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("verified@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        user.setTermsAccepted(true);
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("verified@test.com");
        request.setPassword("password123");

        var response = authService.login(request);

        assertNotNull(response.getToken());
    }
}
