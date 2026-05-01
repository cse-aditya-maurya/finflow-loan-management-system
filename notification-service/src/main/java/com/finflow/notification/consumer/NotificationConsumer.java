package com.finflow.notification.consumer;

import com.finflow.notification.config.RabbitMQConfig;
import com.finflow.notification.dto.NotificationEvent;
import com.finflow.notification.model.NotificationHistory;
import com.finflow.notification.repository.NotificationRepository;
import com.finflow.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * NotificationConsumer - Listens to RabbitMQ loan events and dispatches real HTML emails.
 *
 * Handles:
 *  - LOAN_SUBMITTED: Confirmation to applicant + alert to admin
 *  - LOAN_APPROVED:  Congratulations email to applicant
 *  - LOAN_REJECTED:  Polite rejection notification to applicant
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NOTIFICATION)
    public void consumeNotification(NotificationEvent event) {
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        log.info("📥 EVENT RECEIVED: {}", event.getEventType());
        log.info("   ➜ Application ID : #{}", event.getLoanId());
        log.info("   ➜ Recipient      : {} <{}>", event.getName(), event.getEmail());
        log.info("   ➜ Loan Type      : {}", event.getLoanType());
        log.info("   ➜ Loan Amount    : ₹{}", event.getLoanAmount());
        log.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Save notification record to database
        saveNotificationHistory(event);

        // Route to the appropriate email sender based on event type
        String eventType = event.getEventType();

        if ("LOAN_SUBMITTED".equals(eventType)) {
            handleLoanSubmitted(event);
        } else if ("OTP_CREATED".equals(eventType)) {
            handleOtpCreated(event);
        } else if ("LOAN_APPROVED".equals(eventType)) {
            handleLoanApproved(event);
        } else if ("LOAN_REJECTED".equals(eventType)) {
            handleLoanRejected(event);
        } else {
            log.warn("⚠️ Unknown event type received: {}. No email dispatched.", eventType);
        }

        log.info("✅ Notification processing complete for event: {}", eventType);
    }

    // ──────────────────────────────────────────────────────────────
    // EVENT HANDLERS
    // ──────────────────────────────────────────────────────────────

    /**
     * Sends loan submission confirmation to the user AND a new application alert to admin.
     */
    private void handleLoanSubmitted(NotificationEvent event) {
        log.info("📬 Sending loan submission emails...");

        // 1. Confirm receipt to the applicant
        emailService.sendLoanSubmissionEmailToUser(
                event.getEmail(),
                event.getName(),
                event.getLoanId(),
                event.getLoanType(),
                event.getLoanAmount(),
                event.getTenure()
        );

        // 2. Notify admin about the new pending application
        if (event.getAdminEmail() != null && !event.getAdminEmail().isBlank()) {
            emailService.sendAdminNotificationEmail(
                    event.getAdminEmail(),
                    event.getName(),
                    event.getEmail(),
                    event.getLoanId(),
                    event.getLoanType(),
                    event.getLoanAmount(),
                    event.getTenure()
            );
        } else {
            log.warn("⚠️ No admin email configured in event payload. Skipping admin alert.");
        }
    }

    /**
     * Sends a congratulations approval email to the applicant.
     */
    private void handleLoanApproved(NotificationEvent event) {
        log.info("📬 Sending loan approval email...");
        emailService.sendLoanApprovedEmail(
                event.getEmail(),
                event.getName(),
                event.getLoanId(),
                event.getLoanType(),
                event.getLoanAmount(),
                event.getTenure()
        );
    }

    /**
     * Sends a polite rejection notification email to the applicant.
     */
    private void handleLoanRejected(NotificationEvent event) {
        log.info("📬 Sending loan rejection email...");
        emailService.sendLoanRejectedEmail(
                event.getEmail(),
                event.getName(),
                event.getLoanId(),
                event.getLoanType(),
                event.getLoanAmount()
        );
    }

    /**
     * Sends OTP email to the user.
     */
    private void handleOtpCreated(NotificationEvent event) {
        log.info("📬 Sending OTP email...");
        emailService.sendOtpEmail(
                event.getEmail(),
                event.getName(),
                event.getOtp()
        );
    }

    // ──────────────────────────────────────────────────────────────
    // DATABASE
    // ──────────────────────────────────────────────────────────────

    private void saveNotificationHistory(NotificationEvent event) {
        try {
            NotificationHistory history = NotificationHistory.builder()
                    .eventType(event.getEventType())
                    .recipientEmail(event.getEmail())
                    .recipientName(event.getName())
                    .messageContent(event.getMessage())
                    .sentAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(history);
            log.info("💾 Notification history saved to database.");
        } catch (Exception e) {
            log.error("❌ Failed to save notification history: {}", e.getMessage());
        }
    }
}
