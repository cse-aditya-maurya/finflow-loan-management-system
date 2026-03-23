package com.finflow.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import com.finflow.application.entity.LoanApplication;
import com.finflow.application.repository.LoanApplicationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository repo;

    public LoanApplication create(LoanApplication app) {
        app.setStatus("DRAFT");
        return repo.save(app);
    }

    public LoanApplication submit(Long id) {
        LoanApplication app = repo.findById(id).orElseThrow();
        app.setStatus("SUBMITTED");
        return repo.save(app);
    }
    public void updateStatus(Long id, String status) {

        LoanApplication app = repo.findById(id).orElseThrow();

        app.setStatus(status);

        repo.save(app);
    }

    public List<LoanApplication> myApps(String email) {
        return repo.findByApplicantEmail(email);
    }
    
    public List<LoanApplication> getAll() {
        return repo.findAll();
    }
}