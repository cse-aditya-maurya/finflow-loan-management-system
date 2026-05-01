package com.finflow.application.dto;

import com.finflow.application.model.LoanType;
import com.finflow.application.model.OccupationType;
import lombok.Data;

@Data
public class UpdateApplicationRequest {

    private Double amount;
    private Integer tenure;
    private String employmentType;
    private Double income;

    private LoanType loanType;
    private OccupationType occupation;
    private Integer age;
    private String coApplicantName;
    private Double coApplicantIncome;
    private String coApplicantOccupation;

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

    public LoanType getLoanType() {
        return loanType;
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public OccupationType getOccupation() {
        return occupation;
    }

    public void setOccupation(OccupationType occupation) {
        this.occupation = occupation;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
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


    public UpdateApplicationRequest() {
    }

}
