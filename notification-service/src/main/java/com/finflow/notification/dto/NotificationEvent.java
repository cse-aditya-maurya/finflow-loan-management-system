package com.finflow.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@NoArgsConstructor
//@AllArgsConstructor
@Builder
public class NotificationEvent {
    private String eventType;       // LOAN_SUBMITTED, LOAN_APPROVED, LOAN_REJECTED
    private String email;           // User email
    private String name;            // User display name
    private String message;         // Status message
    private String otp;             // OTP code (used for OTP_CREATED)
    private String documentType;    // Document type (optional)
    private String loanId;          // Loan application ID
    private String applicationId;   // Alias for loanId
    private String loanType;        // e.g. HOME, PERSONAL, EDUCATION
    private Double loanAmount;      // Requested amount
    private Integer tenure;         // Tenure in months
    private String adminEmail;      // Admin email for submission alerts
    private String status;          // Current status

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public Double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(Double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public Integer getTenure() {
        return tenure;
    }

    public void setTenure(Integer tenure) {
        this.tenure = tenure;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public NotificationEvent() {
    }

    public NotificationEvent(String eventType, String email, String name, String message, String otp, String documentType, String loanId, String applicationId, String loanType, Double loanAmount, Integer tenure, String adminEmail, String status) {
        this.eventType = eventType;
        this.email = email;
        this.name = name;
        this.message = message;
        this.otp = otp;
        this.documentType = documentType;
        this.loanId = loanId;
        this.applicationId = applicationId;
        this.loanType = loanType;
        this.loanAmount = loanAmount;
        this.tenure = tenure;
        this.adminEmail = adminEmail;
        this.status = status;
    }

}
