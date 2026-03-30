package com.finflow.application.service;

import com.finflow.application.dto.CreateApplicationRequest;
import com.finflow.application.exception.BadRequestException;
import com.finflow.application.model.*;
import com.finflow.application.repository.LoanApplicationRepository;
import com.finflow.application.dto.UpdateApplicationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository repository;

    @Mock
    private LoanValidationService validationService;

    @InjectMocks
    private LoanApplicationService service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ✅ CREATE SUCCESS
    @Test
    void create_success() {

        CreateApplicationRequest request = new CreateApplicationRequest();
        request.setAmount(500000.0);
        request.setTenure(12);
        request.setIncome(50000.0);
        request.setLoanType(LoanType.HOME);
        request.setAge(25);
        request.setCoApplicantName("Test");
        request.setCoApplicantIncome(20000.0);

        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanApplication result = service.create(1L, request);

        assertEquals(ApplicationStatus.DRAFT, result.getStatus());
        verify(validationService).validate(any());
        verify(repository).save(any());
    }

    // ❌ UPDATE NON-DRAFT
    @Test
    void update_fail_not_draft() {

        LoanApplication app = new LoanApplication();
        app.setUserId(1L);
        app.setStatus(ApplicationStatus.SUBMITTED);

        when(repository.findById(1L)).thenReturn(Optional.of(app));

        assertThrows(BadRequestException.class,
                () -> service.update(1L, 1L, new UpdateApplicationRequest()));
    }

    // ✅ SUBMIT SUCCESS
    @Test
    void submit_success() {

        LoanApplication app = new LoanApplication();
        app.setUserId(1L);
        app.setStatus(ApplicationStatus.DRAFT);

        when(repository.findById(1L)).thenReturn(Optional.of(app));
        when(repository.save(any())).thenReturn(app);

        LoanApplication result = service.submit(1L, 1L);

        assertEquals(ApplicationStatus.SUBMITTED, result.getStatus());
        verify(validationService).validate(app);
    }

    // ❌ SUBMIT ALREADY SUBMITTED
    @Test
    void submit_fail_already_submitted() {

        LoanApplication app = new LoanApplication();
        app.setUserId(1L);
        app.setStatus(ApplicationStatus.SUBMITTED);

        when(repository.findById(1L)).thenReturn(Optional.of(app));

        assertThrows(BadRequestException.class,
                () -> service.submit(1L, 1L));
    }

    // ✅ GET BY ID SUCCESS
    @Test
    void getById_success() {

        LoanApplication app = new LoanApplication();

        when(repository.findById(1L)).thenReturn(Optional.of(app));

        LoanApplication result = service.getById(1L);

        assertNotNull(result);
    }

    // ❌ GET BY ID FAIL
    @Test
    void getById_not_found() {

        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getById(1L));
    }

    // ✅ APPROVE
    @Test
    void approve_success() {

        LoanApplication app = new LoanApplication();
        app.setStatus(ApplicationStatus.SUBMITTED);

        when(repository.findById(1L)).thenReturn(Optional.of(app));

        service.approve(1L);

        assertEquals(ApplicationStatus.APPROVED, app.getStatus());
    }

    // ✅ REJECT
    @Test
    void reject_success() {

        LoanApplication app = new LoanApplication();
        app.setStatus(ApplicationStatus.SUBMITTED);

        when(repository.findById(1L)).thenReturn(Optional.of(app));

        service.reject(1L);

        assertEquals(ApplicationStatus.REJECTED, app.getStatus());
    }
}