package com.finflow.auth.service;

import com.finflow.auth.config.RabbitMQConfig;
import com.finflow.auth.dto.OtpEmailEvent;
import com.finflow.auth.exception.AuthException;
import com.finflow.auth.model.OtpToken;
import com.finflow.auth.model.User;
import com.finflow.auth.repository.OtpTokenRepository;
import com.finflow.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OtpService {

    private static final int OTP_LENGTH = 6;

    private final RabbitTemplate rabbitTemplate;
    private final OtpTokenRepository otpTokenRepository;
    private final UserRepository userRepository;

    @Value("${otp.expiry-minutes:5}")
    private long expiryMinutes;

    @Value("${otp.max-attempts:3}")
    private int maxAttempts;

    private final SecureRandom secureRandom = new SecureRandom();

    public String sendOtp(User user) {
        String email = user.getEmail();

        // Invalidate any previous unused OTPs for the same email.
        otpTokenRepository.deleteAllByEmailAndUsedAtIsNull(email);

        String otp = generateOtp();

        LocalDateTime now = LocalDateTime.now();
        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtp(otp);
        token.setExpiresAt(now.plusMinutes(expiryMinutes));
        token.setAttempts(0);
        token.setUsedAt(null);
        token.setCreatedAt(now);

        otpTokenRepository.save(token);

        publishOtpCreatedEvent(user, otp);
        return otp;
    }

    public User verifyOtpAndActivate(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found"));

        Optional<OtpToken> tokenOpt =
                otpTokenRepository.findTopByEmailAndUsedAtIsNullOrderByCreatedAtDesc(email);

        if (tokenOpt.isEmpty()) {
            throw new AuthException("OTP expired or invalid. Please resend OTP.");
        }

        OtpToken token = tokenOpt.get();
        LocalDateTime now = LocalDateTime.now();

        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(now)) {
            otpTokenRepository.deleteAllByEmailAndUsedAtIsNull(email);
            throw new AuthException("OTP expired. Please resend OTP.");
        }

        if (token.getAttempts() >= maxAttempts) {
            throw new AuthException("Too many invalid attempts. OTP locked. Please resend OTP.");
        }

        if (token.getOtp() != null && token.getOtp().equals(otp)) {
            token.setUsedAt(now);
            otpTokenRepository.save(token);

            // Delete any other unused tokens (resend safety).
            otpTokenRepository.deleteAllByEmailAndUsedAtIsNull(email);

            // "Activate" the account once OTP is verified.
            user.setTermsAccepted(true);
            userRepository.save(user);
            return user;
        }

        int nextAttempts = token.getAttempts() + 1;
        token.setAttempts(nextAttempts);
        otpTokenRepository.save(token);

        if (nextAttempts >= maxAttempts) {
            throw new AuthException("Too many invalid attempts. OTP locked. Please resend OTP.");
        }

        int remaining = maxAttempts - nextAttempts;
        throw new AuthException("Invalid OTP. You have " + remaining + " attempt(s) remaining.");
    }

    public void resendOtp(User user) {
        // If already verified, no need to resend.
        if (user.isTermsAccepted()) {
            return;
        }

        sendOtp(user);
    }

    private String generateOtp() {
        int number = secureRandom.nextInt(1_000_000); // [0..999999]
        return String.format("%0" + OTP_LENGTH + "d", number);
    }

    private void publishOtpCreatedEvent(User user, String otp) {
        // Do not log OTP content to avoid leaking secrets.
        String message = "OTP generated. Please verify within 5 minutes.";

        OtpEmailEvent event = OtpEmailEvent.builder()
                .eventType("OTP_CREATED")
                .email(user.getEmail())
                .name(user.getName())
                .message(message)
                .otp(otp)
                .build();

        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_LOAN,
                    RabbitMQConfig.ROUTING_KEY_AUTH,
                    event
            );
            log.info("OTP event published to RabbitMQ for email: {}", user.getEmail());
        } catch (Exception e) {
            // Fallback: Log OTP to console if RabbitMQ is not available
            log.warn("Failed to publish OTP to RabbitMQ. Logging OTP to console instead.");
            log.info("========================================");
            log.info("OTP for {}: {}", user.getEmail(), otp);
            log.info("Valid for {} minutes", expiryMinutes);
            log.info("========================================");
        }
    }
}

