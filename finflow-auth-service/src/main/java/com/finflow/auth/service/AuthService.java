package com.finflow.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.finflow.auth.dto.LoginRequest;
import com.finflow.auth.dto.SignupRequest;
import com.finflow.auth.entity.User;
import com.finflow.auth.jwt.JwtUtil;
import com.finflow.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public void signup(SignupRequest req) {

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(encoder.encode(req.getPassword()))
                .role("APPLICANT")
                .active(true)
                .build();

        repo.save(user);
    }

    public String login(LoginRequest req) {

        User user = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid Email"));

        if (!encoder.matches(req.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid Password");

        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
}