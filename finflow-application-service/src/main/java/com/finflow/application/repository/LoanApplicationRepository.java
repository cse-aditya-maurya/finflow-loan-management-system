package com.finflow.application.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.application.entity.LoanApplication;

public interface LoanApplicationRepository 
extends JpaRepository<LoanApplication, Long> {

List<LoanApplication> findByApplicantEmail(String email);
}