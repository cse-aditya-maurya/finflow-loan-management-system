package com.finflow.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * EmailService - Sends beautifully formatted HTML emails for all loan lifecycle events.
 *
 * Handles three scenarios:
 *  1. LOAN_SUBMITTED - Sends confirmation to user + alert to admin
 *  2. LOAN_APPROVED  - Sends congratulations email to user
 *  3. LOAN_REJECTED  - Sends polite rejection email to user
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ──────────────────────────────────────────────
    // PUBLIC API
    // ──────────────────────────────────────────────

    /**
     * Send a loan submission confirmation to the user.
     */
    public void sendLoanSubmissionEmailToUser(String toEmail, String userName,
                                               String applicationId, String loanType,
                                               Double amount, Integer tenure) {
        try {
            String subject = "✅ Loan Application Received – FinFlow";
            String htmlBody = buildSubmissionEmailHtml(userName, applicationId, loanType, amount, tenure);
            sendHtmlEmail(toEmail, subject, htmlBody);
            log.info("📧 Loan submission confirmation sent to user: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send submission email to user {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send a new loan submission alert to the admin.
     */
    public void sendLoanSubmissionEmailToAdmin(String adminEmail, String userName,
                                                String userEmail, String applicationId,
                                                String loanType, Double amount, Integer tenure) {
        try {
            String subject = "🔔 New Loan Application Received – Action Required";
            String htmlBody = buildAdminAlertEmailHtml(userName, userEmail, applicationId, loanType, amount, tenure);
            sendHtmlEmail(adminEmail, subject, htmlBody);
            log.info("📧 New loan application alert sent to admin: {}", adminEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send admin alert email for application {}: {}", applicationId, e.getMessage());
        }
    }

    /**
     * Alias for admin notification email (kept for clearer API naming).
     */
    public void sendAdminNotificationEmail(String adminEmail, String userName,
                                            String userEmail, String applicationId,
                                            String loanType, Double amount, Integer tenure) {
        sendLoanSubmissionEmailToAdmin(adminEmail, userName, userEmail, applicationId, loanType, amount, tenure);
    }

    /**
     * Send a loan approval congratulations email to the user.
     */
    public void sendLoanApprovedEmail(String toEmail, String userName,
                                       String applicationId, String loanType,
                                       Double amount, Integer tenure) {
        try {
            String subject = "🎉 Congratulations! Your Loan is Approved – FinFlow";
            String htmlBody = buildApprovalEmailHtml(userName, applicationId, loanType, amount, tenure);
            sendHtmlEmail(toEmail, subject, htmlBody);
            log.info("📧 Loan approval email sent to user: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send approval email to user {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send a loan rejection notification email to the user.
     */
    public void sendLoanRejectedEmail(String toEmail, String userName,
                                       String applicationId, String loanType, Double amount) {
        try {
            String subject = "📋 Update on Your Loan Application – FinFlow";
            String htmlBody = buildRejectionEmailHtml(userName, applicationId, loanType, amount);
            sendHtmlEmail(toEmail, subject, htmlBody);
            log.info("📧 Loan rejection email sent to user: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send rejection email to user {}: {}", toEmail, e.getMessage());
        }
    }

    /**
     * Send a one-time-password (OTP) email for account verification.
     */
    public void sendOtpEmail(String toEmail, String userName, String otp) {
        try {
            String subject = "FinFlow OTP Verification Code";
            String htmlBody = buildOtpEmailHtml(userName, otp);
            String plainText = buildOtpEmailText(userName, otp);
            sendEmail(toEmail, subject, plainText, htmlBody);
            log.info("📧 OTP email sent to user: {}", toEmail);
        } catch (Exception e) {
            log.error("❌ Failed to send OTP email to user {}: {}", toEmail, e.getMessage());
        }
    }

    // ──────────────────────────────────────────────
    // PRIVATE HELPERS
    // ──────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody)
            throws MessagingException, java.io.UnsupportedEncodingException {
        sendEmail(to, subject, "", htmlBody);
    }

    private void sendEmail(String to, String subject, String plainTextBody, String htmlBody)
            throws MessagingException, java.io.UnsupportedEncodingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        // fromEmail is @Value-injected and guaranteed non-null at runtime
        helper.setFrom(fromEmail != null ? fromEmail : "noreply@finflow.com", "FinFlow Loan Services");
        helper.setTo(to != null ? to : "");
        helper.setSubject(subject != null ? subject : "FinFlow Notification");
        helper.setText(
                plainTextBody != null ? plainTextBody : "",
                htmlBody != null ? htmlBody : ""
        );
        mailSender.send(message);
    }

    private String formatCurrency(Double amount) {
        if (amount == null) return "N/A";
        // Use new Locale for Java 17 compatibility
        return "\u20B9" + NumberFormat.getNumberInstance(new Locale("en", "IN")).format(amount);
    }

    private String formatTenure(Integer tenure) {
        if (tenure == null) return "N/A";
        return tenure + " Months (" + (tenure / 12) + " yrs " + (tenure % 12) + " mos)";
    }

    private String getHeaderColor(String eventType) {
        return switch (eventType) {
            case "APPROVED" -> "linear-gradient(135deg, #1a7a4a 0%, #25a665 100%)";
            case "REJECTED" -> "linear-gradient(135deg, #b91c1c 0%, #ef4444 100%)";
            default -> "linear-gradient(135deg, #1e40af 0%, #3b82f6 100%)";
        };
    }

    // ──────────────────────────────────────────────
    // HTML TEMPLATE: Shared header/footer wrappers
    // ──────────────────────────────────────────────

    private String wrapInLayout(String headerGradient, String headerIcon,
                                 String headerTitle, String bodyContent) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>FinFlow Notification</title>
            </head>
            <body style="margin:0;padding:0;background:#f0f4f8;font-family:'Segoe UI',Helvetica,Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="background:#f0f4f8;padding:30px 0;">
                <tr><td align="center">
                  <table width="620" cellpadding="0" cellspacing="0" style="max-width:620px;width:100%%;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">

                    <!-- HEADER -->
                    <tr>
                      <td style="background:%s;padding:36px 40px;text-align:center;">
                        <div style="font-size:48px;margin-bottom:12px;">%s</div>
                        <h1 style="color:#ffffff;margin:0;font-size:24px;font-weight:700;letter-spacing:-0.3px;">%s</h1>
                        <p style="color:rgba(255,255,255,0.85);margin:8px 0 0;font-size:13px;font-weight:500;letter-spacing:1px;text-transform:uppercase;">FinFlow Loan Management System</p>
                      </td>
                    </tr>

                    <!-- BODY -->
                    <tr>
                      <td style="padding:36px 40px;">
                        %s
                      </td>
                    </tr>

                    <!-- FOOTER -->
                    <tr>
                      <td style="background:#f8fafc;border-top:1px solid #e2e8f0;padding:24px 40px;text-align:center;">
                        <p style="color:#94a3b8;font-size:12px;margin:0 0 6px;">© 2025 FinFlow Technologies Pvt. Ltd. · All Rights Reserved</p>
                        <p style="color:#94a3b8;font-size:12px;margin:0;">This is an automated message. Please do not reply to this email.</p>
                        <p style="margin:12px 0 0;font-size:11px;color:#cbd5e1;">Need help? Contact us at <a href="mailto:support@finflow.com" style="color:#3b82f6;text-decoration:none;">support@finflow.com</a></p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(headerGradient, headerIcon, headerTitle, bodyContent);
    }

    private String detailTable(String... rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"border-radius:10px;overflow:hidden;border:1px solid #e2e8f0;margin:24px 0;\">");
        for (int i = 0; i < rows.length; i += 2) {
            String label = rows[i];
            String value = (i + 1 < rows.length) ? rows[i + 1] : "";
            String rowBg = (i / 2 % 2 == 0) ? "#f8fafc" : "#ffffff";
            sb.append("""
                <tr style="background:%s;">
                  <td style="padding:12px 16px;font-size:13px;color:#64748b;font-weight:600;width:40%%;border-right:1px solid #e2e8f0;">%s</td>
                  <td style="padding:12px 16px;font-size:14px;color:#1e293b;font-weight:500;">%s</td>
                </tr>
                """.formatted(rowBg, label, value));
        }
        sb.append("</table>");
        return sb.toString();
    }

    // ──────────────────────────────────────────────
    // TEMPLATE: User – Loan Submitted
    // ──────────────────────────────────────────────

    private String buildSubmissionEmailHtml(String userName, String applicationId,
                                             String loanType, Double amount, Integer tenure) {
        String gradient = getHeaderColor("SUBMITTED");
        String details = detailTable(
            "Application ID",    "#" + applicationId,
            "Loan Type",         loanType,
            "Requested Amount",  formatCurrency(amount),
            "Loan Tenure",       formatTenure(tenure),
            "Current Status",    "<span style='color:#f59e0b;font-weight:700;'>⏳ UNDER REVIEW</span>"
        );

        String body = """
            <p style="color:#1e293b;font-size:16px;margin:0 0 8px;">Dear <strong>%s</strong>,</p>
            <p style="color:#475569;font-size:15px;line-height:1.7;margin:0 0 24px;">
              Thank you for submitting your loan application with <strong>FinFlow</strong>.
              We have successfully received your application and our team will review it shortly.
            </p>

            <h3 style="color:#1e293b;font-size:15px;margin:0 0 4px;font-weight:700;">📋 Application Summary</h3>
            %s

            <div style="background:linear-gradient(135deg,#eff6ff,#dbeafe);border-left:4px solid #3b82f6;border-radius:8px;padding:16px 20px;margin:24px 0;">
              <p style="color:#1e40af;font-size:14px;margin:0;font-weight:600;">⏱️ What happens next?</p>
              <ul style="color:#1e40af;font-size:13px;margin:10px 0 0;padding-left:20px;line-height:1.8;">
                <li>Our team will review your documents within 2–3 business days.</li>
                <li>You will receive an email notification once a decision is made.</li>
                <li>You can check your application status anytime on the FinFlow portal.</li>
              </ul>
            </div>

            <p style="color:#64748b;font-size:13px;margin:24px 0 0;line-height:1.7;">
              If you have any questions or need to make changes, please log in to your account or contact our support team.
            </p>
            """.formatted(userName, details);

        return wrapInLayout(gradient, "📄", "Application Received!", body);
    }

    // ──────────────────────────────────────────────
    // TEMPLATE: Admin – New Loan Application Alert
    // ──────────────────────────────────────────────

    private String buildAdminAlertEmailHtml(String userName, String userEmail,
                                             String applicationId, String loanType,
                                             Double amount, Integer tenure) {
        String gradient = "linear-gradient(135deg, #7c3aed 0%, #a78bfa 100%)";
        String details = detailTable(
            "Application ID",    "#" + applicationId,
            "Applicant Name",    userName,
            "Applicant Email",   userEmail,
            "Loan Type",         loanType,
            "Requested Amount",  formatCurrency(amount),
            "Loan Tenure",       formatTenure(tenure),
            "Status",            "<span style='color:#f59e0b;font-weight:700;'>⏳ PENDING REVIEW</span>"
        );

        String body = """
            <p style="color:#1e293b;font-size:16px;margin:0 0 8px;">Hello <strong>Admin</strong>,</p>
            <p style="color:#475569;font-size:15px;line-height:1.7;margin:0 0 24px;">
              A new loan application has been submitted on <strong>FinFlow</strong> and is awaiting your review. Please log in to the admin panel to take action.
            </p>

            <h3 style="color:#1e293b;font-size:15px;margin:0 0 4px;font-weight:700;">📋 Application Details</h3>
            %s

            <div style="background:linear-gradient(135deg,#faf5ff,#ede9fe);border-left:4px solid #7c3aed;border-radius:8px;padding:16px 20px;margin:24px 0;">
              <p style="color:#6d28d9;font-size:14px;margin:0;font-weight:600;">🔔 Action Required</p>
              <p style="color:#6d28d9;font-size:13px;margin:10px 0 0;line-height:1.7;">
                Please review the applicant's documents and financial details in the admin portal and approve or reject the application within 2–3 business days.
              </p>
            </div>
            """.formatted(details);

        return wrapInLayout(gradient, "🔔", "New Loan Application Alert", body);
    }

    // ──────────────────────────────────────────────
    // TEMPLATE: User – Loan Approved
    // ──────────────────────────────────────────────

    private String buildApprovalEmailHtml(String userName, String applicationId,
                                           String loanType, Double amount, Integer tenure) {
        String gradient = getHeaderColor("APPROVED");
        String details = detailTable(
            "Application ID",    "#" + applicationId,
            "Loan Type",         loanType,
            "Approved Amount",   formatCurrency(amount),
            "Loan Tenure",       formatTenure(tenure),
            "Status",            "<span style='color:#16a34a;font-weight:700;'>✅ APPROVED</span>"
        );

        String body = """
            <p style="color:#1e293b;font-size:16px;margin:0 0 8px;">Dear <strong>%s</strong>,</p>
            <p style="color:#475569;font-size:15px;line-height:1.7;margin:0 0 24px;">
              We are delighted to inform you that your loan application has been <strong style="color:#16a34a;">APPROVED</strong>! 🎉
              Congratulations on this milestone. The funds will be processed and disbursed to your registered bank account shortly.
            </p>

            <h3 style="color:#1e293b;font-size:15px;margin:0 0 4px;font-weight:700;">📋 Loan Approval Details</h3>
            %s

            <div style="background:linear-gradient(135deg,#f0fdf4,#dcfce7);border-left:4px solid #16a34a;border-radius:8px;padding:16px 20px;margin:24px 0;">
              <p style="color:#166534;font-size:14px;margin:0;font-weight:600;">💰 Next Steps for Disbursement</p>
              <ul style="color:#166534;font-size:13px;margin:10px 0 0;padding-left:20px;line-height:1.8;">
                <li>Funds will be disbursed to your registered bank account within 3–5 business days.</li>
                <li>You will receive an SMS confirmation upon successful disbursal.</li>
                <li>Your EMI schedule will be available in your FinFlow account dashboard.</li>
                <li>For any queries, reach us at <strong>support@finflow.com</strong>.</li>
              </ul>
            </div>

            <p style="color:#64748b;font-size:13px;margin:24px 0 0;line-height:1.7;">
              Thank you for choosing FinFlow. We look forward to supporting your financial journey!
            </p>
            """.formatted(userName, details);

        return wrapInLayout(gradient, "🎉", "Your Loan is Approved!", body);
    }

    // ──────────────────────────────────────────────
    // TEMPLATE: User – Loan Rejected
    // ──────────────────────────────────────────────

    private String buildRejectionEmailHtml(String userName, String applicationId,
                                            String loanType, Double amount) {
        String gradient = getHeaderColor("REJECTED");
        String details = detailTable(
            "Application ID",   "#" + applicationId,
            "Loan Type",        loanType,
            "Requested Amount", formatCurrency(amount),
            "Status",           "<span style='color:#dc2626;font-weight:700;'>❌ NOT APPROVED</span>"
        );

        String body = """
            <p style="color:#1e293b;font-size:16px;margin:0 0 8px;">Dear <strong>%s</strong>,</p>
            <p style="color:#475569;font-size:15px;line-height:1.7;margin:0 0 24px;">
              We regret to inform you that, after a careful review of your loan application,
              we are unable to approve your request at this time. Please know that this decision
              was not taken lightly, and we understand this may be disappointing news.
            </p>

            <h3 style="color:#1e293b;font-size:15px;margin:0 0 4px;font-weight:700;">📋 Application Details</h3>
            %s

            <div style="background:linear-gradient(135deg,#fef2f2,#fee2e2);border-left:4px solid #dc2626;border-radius:8px;padding:16px 20px;margin:24px 0;">
              <p style="color:#991b1b;font-size:14px;margin:0;font-weight:600;">ℹ️ Common Reasons for Rejection</p>
              <ul style="color:#991b1b;font-size:13px;margin:10px 0 0;padding-left:20px;line-height:1.8;">
                <li>Insufficient credit score or credit history.</li>
                <li>High existing debt-to-income ratio.</li>
                <li>Incomplete or inconsistent documentation.</li>
                <li>Income not meeting the minimum eligibility criteria.</li>
              </ul>
            </div>

            <div style="background:#f8fafc;border-radius:10px;padding:20px;margin:24px 0;border:1px solid #e2e8f0;">
              <p style="color:#374151;font-size:14px;margin:0 0 8px;font-weight:600;">🔄 What can you do?</p>
              <p style="color:#6b7280;font-size:13px;margin:0;line-height:1.8;">
                You are welcome to re-apply after 90 days or after improving your financial profile.
                Our support team can guide you on steps to strengthen your application.
                Contact us at <a href="mailto:support@finflow.com" style="color:#3b82f6;text-decoration:none;">support@finflow.com</a>.
              </p>
            </div>

            <p style="color:#64748b;font-size:13px;margin:24px 0 0;line-height:1.7;">
              We appreciate your trust in FinFlow and hope to assist you better in the future. Wishing you all the best.
            </p>
            """.formatted(userName, details);

        return wrapInLayout(gradient, "📋", "Application Status Update", body);
    }

    // ──────────────────────────────────────────────
    // TEMPLATE: User – OTP Verification
    // ──────────────────────────────────────────────
    private String buildOtpEmailHtml(String userName, String otp) {
        String gradient = "linear-gradient(135deg, #0284c7 0%, #2563eb 100%)";

        String safeOtp = (otp == null || otp.isBlank()) ? "—" : otp.trim();

        String body = """
            <p style="color:#1e293b;font-size:16px;margin:0 0 8px;">Hello <strong>%s</strong>,</p>
            <p style="color:#475569;font-size:15px;line-height:1.7;margin:0 0 24px;">
              Use the following one-time password (OTP) to verify your FinFlow account.
              This OTP will expire in <strong>5 minutes</strong>.
            </p>

            <div style="margin:22px 0;background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:18px 20px;text-align:center;">
              <div style="color:#64748b;font-size:12px;letter-spacing:1px;text-transform:uppercase;font-weight:700;">Your OTP</div>
              <div style="font-size:34px;font-weight:900;color:#1d4ed8;letter-spacing:6px;margin:10px 0 0;">%s</div>
              <div style="color:#94a3b8;font-size:12px;margin-top:10px;">If you didn't request this, you can ignore this email.</div>
            </div>

            <p style="color:#64748b;font-size:13px;margin:24px 0 0;line-height:1.7;">
              For your security, never share this OTP with anyone.
            </p>
            """.formatted(userName, safeOtp);

        return wrapInLayout(gradient, "🔐", "Verify Your OTP", body);
    }

    private String buildOtpEmailText(String userName, String otp) {
        String safeName = (userName == null || userName.isBlank()) ? "User" : userName.trim();
        String safeOtp = (otp == null || otp.isBlank()) ? "N/A" : otp.trim();
        return """
                Hello %s,

                Your FinFlow OTP is: %s

                This OTP expires in 5 minutes.
                Do not share this OTP with anyone.

                If you did not request this OTP, please ignore this email.
                """
                .formatted(safeName, safeOtp);
    }
}
