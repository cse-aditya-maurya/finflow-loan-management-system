package com.finflow.application.service;

import com.finflow.application.client.DocumentClient;
import com.finflow.application.dto.CreateApplicationRequest;
import com.finflow.application.dto.UpdateApplicationRequest;
import com.finflow.application.exception.BadRequestException;
import com.finflow.application.model.ApplicationStatus;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.repository.LoanApplicationRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class LoanApplicationService {

    private final LoanApplicationRepository repository;
    private final LoanValidationService validationService;
    private final DocumentClient documentClient;

    public LoanApplicationService(LoanApplicationRepository repository,
            LoanValidationService validationService,
            DocumentClient documentClient) {
        this.repository = repository;
        this.validationService = validationService;
        this.documentClient = documentClient;
    }

    // ===============================
    // 🔹 COMMON METHOD
    // ===============================
    public LoanApplication getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BadRequestException("Application not found"));
    }

    // ===============================
    // ✅ CREATE APPLICATION
    // ===============================
    public LoanApplication create(Long userId, CreateApplicationRequest request) {

        // 🔥 Validate no active application for this loan type
        validateNoActiveApplication(userId, request.getLoanType(), null);

        LoanApplication app = new LoanApplication();

        app.setUserId(userId);
        app.setAmount(request.getAmount());
        app.setTenure(request.getTenure());
        app.setIncome(request.getIncome());
        app.setLoanType(request.getLoanType());
        app.setOccupation(request.getOccupation());
        app.setAge(request.getAge());
        app.setCoApplicantName(request.getCoApplicantName());
        app.setCoApplicantIncome(request.getCoApplicantIncome());
        app.setCoApplicantOccupation(request.getCoApplicantOccupation());

        app.setStatus(ApplicationStatus.DRAFT);
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());

        validationService.validate(app);

        return repository.save(app);
    }

    // ===============================
    // ✅ UPDATE (ONLY DRAFT)
    // ===============================
    public LoanApplication update(Long userId, Long id, UpdateApplicationRequest request) {

        LoanApplication app = getById(id);

        if (!app.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized: You do not own this application");
        }

        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Only draft applications can be updated");
        }

        // 🔥 Validate no active application if loan type is being changed
        if (request.getLoanType() != null && !request.getLoanType().equals(app.getLoanType())) {
            validateNoActiveApplication(userId, request.getLoanType(), id);
        }

        if (request.getAmount() != null)
            app.setAmount(request.getAmount());
        if (request.getTenure() != null)
            app.setTenure(request.getTenure());
        if (request.getIncome() != null)
            app.setIncome(request.getIncome());
        if (request.getLoanType() != null)
            app.setLoanType(request.getLoanType());
        if (request.getOccupation() != null)
            app.setOccupation(request.getOccupation());
        if (request.getAge() != null)
            app.setAge(request.getAge());
        if (request.getCoApplicantName() != null)
            app.setCoApplicantName(request.getCoApplicantName());
        if (request.getCoApplicantIncome() != null)
            app.setCoApplicantIncome(request.getCoApplicantIncome());
        if (request.getCoApplicantOccupation() != null)
            app.setCoApplicantOccupation(request.getCoApplicantOccupation());

        app.setUpdatedAt(LocalDateTime.now());

        return repository.save(app);
    }

    // ===============================
    // ✅ SUBMIT
    // ===============================
    public LoanApplication submit(Long userId, Long id) {

        LoanApplication app = getById(id);

        if (!app.getUserId().equals(userId)) {
            throw new BadRequestException("Unauthorized: You do not own this application");
        }

        if (app.getStatus() != ApplicationStatus.DRAFT) {
            throw new BadRequestException("Application already submitted");
        }

        // 📝 Verify Documents before Submission
        boolean docsComplete = documentClient.validateDocuments(id, app.getLoanType());
        if (!docsComplete) {
            throw new BadRequestException("Please upload the documents first.");
        }

        validationService.validate(app);

        app.setStatus(ApplicationStatus.SUBMITTED);
        app.setUpdatedAt(LocalDateTime.now());

        return repository.save(app);
    }

    // ===============================
    // ✅ GET USER APPLICATIONS
    // ===============================
    public List<LoanApplication> getByUser(Long userId) {
        return repository.findByUserId(userId);
    }

    // ===============================
    // ✅ GET STATUS
    // ===============================
    public String getStatus(Long id) {
        return getById(id).getStatus().name();
    }

    // ===============================
    // 🔥 ADMIN APPROVE
    // ===============================
    public Map<String, String> approve(Long id) {

        LoanApplication app = getById(id);

        if (app.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new BadRequestException("Application must be SUBMITTED first");
        }

        app.setStatus(ApplicationStatus.APPROVED);
        app.setUpdatedAt(LocalDateTime.now());

        repository.save(app);

        return Map.of("message", "Application approved");
    }

    // ===============================
    // 🔥 ADMIN REJECT
    // ===============================
    public Map<String, String> reject(Long id) {

        LoanApplication app = getById(id);

        if (app.getStatus() != ApplicationStatus.SUBMITTED) {
            throw new BadRequestException("Application must be SUBMITTED first");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setUpdatedAt(LocalDateTime.now());

        repository.save(app);

        return Map.of("message", "Application rejected");
    }

    // ===============================
    // ✅ GET ALL
    // ===============================
    public List<LoanApplication> getAll() {
        return repository.findAll().stream()
                .filter(app -> app.getStatus() != ApplicationStatus.DRAFT)
                .toList();
    }
    // ===============================
    // ✅ VALIDATE NO ACTIVE APP
    // ===============================
    private void validateNoActiveApplication(Long userId, com.finflow.application.model.LoanType loanType, Long currentAppId) {

        List<LoanApplication> existingApps = repository.findByUserIdAndLoanType(userId, loanType);

        boolean hasActive = existingApps.stream()
                .filter(app -> currentAppId == null || !app.getId().equals(currentAppId))
                .anyMatch(app -> app.getStatus() == ApplicationStatus.DRAFT ||
                        app.getStatus() == ApplicationStatus.SUBMITTED);

        if (hasActive) {
            throw new BadRequestException("You already have an active application for this loan type");
        }
    }
}