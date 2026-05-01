package com.finflow.application.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Double amount;
    private Integer tenure;
    private String employmentType;
    private Double income;

    @Enumerated(EnumType.STRING)
    private LoanType loanType;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private OccupationType occupation;
    
    // Co-applicant
    private String coApplicantName;
    private Double coApplicantIncome;
    private String coApplicantOccupation;

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Integer getTenure() {
        return tenure;
    }

    public void setTenure(Integer tenure) {
        this.tenure = tenure;
    }

    public String getEmploymentType() {
        return employmentType;
    }

    public void setEmploymentType(String employmentType) {
        this.employmentType = employmentType;
    }

    public Double getIncome() {
        return income;
    }

    public void setIncome(Double income) {
        this.income = income;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public OccupationType getOccupation() {
        return occupation;
    }

    public void setOccupation(OccupationType occupation) {
        this.occupation = occupation;
    }

    public String getCoApplicantName() {
        return coApplicantName;
    }

    public void setCoApplicantName(String coApplicantName) {
        this.coApplicantName = coApplicantName;
    }

    public Double getCoApplicantIncome() {
        return coApplicantIncome;
    }

    public void setCoApplicantIncome(Double coApplicantIncome) {
        this.coApplicantIncome = coApplicantIncome;
    }

    public String getCoApplicantOccupation() {
        return coApplicantOccupation;
    }

    public void setCoApplicantOccupation(String coApplicantOccupation) {
        this.coApplicantOccupation = coApplicantOccupation;
    }


    public LoanApplication() {
    }

}
