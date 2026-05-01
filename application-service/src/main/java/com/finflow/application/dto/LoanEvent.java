//package com.finflow.application.dto;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//public class LoanEvent {
//    private String eventType;       // LOAN_SUBMITTED, LOAN_APPROVED, LOAN_REJECTED
//    private String email;           // User's email
//    private String name;            // User's display name
//    private String message;         // Notification message
//    private String loanId;          // Loan application ID
//    private String status;          // Current status
//    private String loanType;        // e.g. HOME, PERSONAL, EDUCATION
//    private Double loanAmount;      // Requested loan amount
//    private Integer tenure;         // Loan tenure in months
//    private String adminEmail;      // Admin email for new submission alerts
//
//    public String getEventType() {
//        return eventType;
//    }
//
//    public void setEventType(String eventType) {
//        this.eventType = eventType;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getMessage() {
//        return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
//
//    public String getLoanId() {
//        return loanId;
//    }
//
//    public void setLoanId(String loanId) {
//        this.loanId = loanId;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//    public String getLoanType() {
//        return loanType;
//    }
//
//    public void setLoanType(String loanType) {
//        this.loanType = loanType;
//    }
//
//    public Double getLoanAmount() {
//        return loanAmount;
//    }
//
//    public void setLoanAmount(Double loanAmount) {
//        this.loanAmount = loanAmount;
//    }
//
//    public Integer getTenure() {
//        return tenure;
//    }
//
//    public void setTenure(Integer tenure) {
//        this.tenure = tenure;
//    }
//
//    public String getAdminEmail() {
//        return adminEmail;
//    }
//
//    public void setAdminEmail(String adminEmail) {
//        this.adminEmail = adminEmail;
//    }
//
//
//    public LoanEvent() {
//    }
//
//    public LoanEvent(String eventType, String email, String name, String message, String loanId, String status, String loanType, Double loanAmount, Integer tenure, String adminEmail) {
//        this.eventType = eventType;
//        this.email = email;
//        this.name = name;
//        this.message = message;
//        this.loanId = loanId;
//        this.status = status;
//        this.loanType = loanType;
//        this.loanAmount = loanAmount;
//        this.tenure = tenure;
//        this.adminEmail = adminEmail;
//    }
//
//}


















package com.finflow.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanEvent {
    private String eventType;
    private String email;
    private String name;
    private String message;
    private String loanId;
    private String status;
    private String loanType;
    private Double loanAmount;
    private Integer tenure;
    private String adminEmail;
}
