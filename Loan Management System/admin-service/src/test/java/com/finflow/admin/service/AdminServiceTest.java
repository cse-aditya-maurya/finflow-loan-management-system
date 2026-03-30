package com.finflow.admin.service;

import com.finflow.admin.client.ApplicationClient;
import com.finflow.admin.client.DocumentClient;
import com.finflow.admin.model.Decision;
import com.finflow.admin.model.Report;
import com.finflow.admin.repository.DecisionRepository;
import com.finflow.admin.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private ApplicationClient applicationClient;

    @Mock
    private DocumentClient documentClient;

    @Mock
    private DecisionRepository decisionRepo;

    @Mock
    private ReportRepository reportRepo;

    @InjectMocks
    private AdminService adminService;

    private final String token = "Bearer test-token";
    private final Long appId = 1L;
    private final Long adminId = 100L;
    private final Long docId = 1000L;

    // ===============================
    // ✅ GET APPLICATIONS TESTS
    // ===============================

    @Test
    void getApplications_ShouldReturnOnlyNonDraftApplications() {
        // Arrange
        List<Map<String, Object>> allApps = List.of(
            Map.of("id", 1, "status", "SUBMITTED"),
            Map.of("id", 2, "status", "DRAFT"),
            Map.of("id", 3, "status", "APPROVED"),
            Map.of("id", 4, "status", "REJECTED"),
            Map.of("id", 5, "status", "draft") // lowercase draft
        );
        when(applicationClient.getApplications(token)).thenReturn(allApps);

        // Act
        List<Map<String, Object>> result = adminService.getApplications(token);

        // Assert
        assertEquals(3, result.size());
        assertEquals(1L, result.get(0).get("id"));
        assertEquals(3L, result.get(1).get("id"));
        assertEquals(4L, result.get(2).get("id"));
        verify(applicationClient, times(1)).getApplications(token);
    }

    @Test
    void getApplications_WhenNoApplications_ShouldReturnEmptyList() {
        // Arrange
        when(applicationClient.getApplications(token)).thenReturn(List.of());

        // Act
        List<Map<String, Object>> result = adminService.getApplications(token);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getApplications_WhenAllAreDraft_ShouldReturnEmptyList() {
        // Arrange
        List<Map<String, Object>> allApps = List.of(
            Map.of("id", 1, "status", "DRAFT"),
            Map.of("id", 2, "status", "DRAFT")
        );
        when(applicationClient.getApplications(token)).thenReturn(allApps);

        // Act
        List<Map<String, Object>> result = adminService.getApplications(token);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ===============================
    // ✅ GET APPLICATION BY ID TESTS
    // ===============================

    @Test
    void getApplicationById_WithNonDraftStatus_ShouldReturnApplication() {
        // Arrange
        Map<String, Object> app = Map.of("id", appId, "status", "SUBMITTED", "loanType", "PERSONAL");
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);

        // Act
        Map<String, Object> result = adminService.getApplicationById(appId, token);

        // Assert
        assertNotNull(result);
        assertEquals(appId, result.get("id"));
        assertEquals("SUBMITTED", result.get("status"));
        verify(applicationClient, times(1)).getApplicationById(token, appId);
    }

    @Test
    void getApplicationById_WithDraftStatus_ShouldThrowException() {
        // Arrange
        Map<String, Object> app = Map.of("id", appId, "status", "DRAFT");
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.getApplicationById(appId, token));
        
        assertEquals("Application not found", exception.getMessage());
    }

    @Test
    void getApplicationById_WithLowerCaseDraftStatus_ShouldThrowException() {
        // Arrange
        Map<String, Object> app = Map.of("id", appId, "status", "draft");
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.getApplicationById(appId, token));
        
        assertEquals("Application not found", exception.getMessage());
    }

    // ===============================
    // ✅ GET DOCUMENTS TESTS
    // ===============================

    @Test
    void getDocuments_ShouldReturnDocumentsList() {
        // Arrange
        List<Map<String, Object>> docs = List.of(
            Map.of("id", 1, "status", "VERIFIED"),
            Map.of("id", 2, "status", "UPLOADED")
        );
        when(documentClient.getDocuments(appId, token)).thenReturn(docs);

        // Act
        List<Map<String, Object>> result = adminService.getDocuments(appId, token);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(documentClient, times(1)).getDocuments(appId, token);
    }

    // ===============================
    // ✅ VERIFY DOCUMENT TESTS
    // ===============================

    @Test
    void verifyDocument_ShouldReturnVerificationResponse() {
        // Arrange
        Map<String, String> verificationResponse = Map.of("status", "VERIFIED", "message", "Document verified");
        when(documentClient.verify(docId, token)).thenReturn(verificationResponse);

        // Act
        Object result = adminService.verifyDocument(docId, token);

        // Assert
        assertNotNull(result);
        verify(documentClient, times(1)).verify(docId, token);
    }

    // ===============================
    // ✅ REJECT DOCUMENT TESTS
    // ===============================

    @Test
    void rejectDocument_ShouldReturnRejectionResponse() {
        // Arrange
        String remarks = "Document is blurry";
        Map<String, String> rejectionResponse = Map.of("status", "REJECTED", "message", "Document rejected");
        when(documentClient.reject(docId, remarks, token)).thenReturn(rejectionResponse);

        // Act
        Object result = adminService.rejectDocument(docId, remarks, token);

        // Assert
        assertNotNull(result);
        verify(documentClient, times(1)).reject(docId, remarks, token);
    }

    // ===============================
    // ✅ APPROVE APPLICATION TESTS
    // ===============================

    @Test
    void approve_WhenAllConditionsMet_ShouldApproveSuccessfully() {
        // Arrange
        Map<String, Object> app = Map.of("status", "SUBMITTED", "loanType", "PERSONAL");
        List<Map<String, Object>> docs = List.of(
            Map.of("status", "VERIFIED"),
            Map.of("status", "VERIFIED")
        );
        Map<String, String> approvalResponse = Map.of("status", "APPROVED", "message", "Application approved");

        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);
        when(documentClient.getDocuments(appId, token)).thenReturn(docs);
        when(documentClient.validateDocuments(appId, "PERSONAL", token)).thenReturn(true);
        when(applicationClient.approve(appId, token)).thenReturn(approvalResponse);
        when(decisionRepo.save(any(Decision.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, String> result = adminService.approve(appId, adminId, token);

        // Assert
        assertNotNull(result);
        assertEquals("APPROVED", result.get("status"));
        
        // Verify decision saved
        ArgumentCaptor<Decision> decisionCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepo, times(1)).save(decisionCaptor.capture());
        
        Decision savedDecision = decisionCaptor.getValue();
        assertEquals(appId, savedDecision.getApplicationId());
        assertEquals(adminId, savedDecision.getAdminId());
        assertEquals("APPROVED", savedDecision.getDecision());
        assertNotNull(savedDecision.getCreatedAt());
    }

    @Test
    void approve_WhenApplicationNotSubmitted_ShouldThrowException() {
        // Arrange
        Map<String, Object> app = Map.of("status", "DRAFT");
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.approve(appId, adminId, token));
        
        assertEquals("Application missing", exception.getMessage());
        verify(decisionRepo, never()).save(any());
        verify(applicationClient, never()).approve(any(), any());
    }

    @Test
    void approve_WhenNoDocumentsFound_ShouldThrowException() {
        // Arrange
        Map<String, Object> app = Map.of("status", "SUBMITTED", "loanType", "PERSONAL");
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);
        when(documentClient.getDocuments(appId, token)).thenReturn(List.of());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.approve(appId, adminId, token));
        
        assertEquals("Firstly verify the documents.", exception.getMessage());
        verify(decisionRepo, never()).save(any());
    }

    @Test
    void approve_WhenDocumentsNotAllVerified_ShouldThrowException() {
        // Arrange
        Map<String, Object> app = Map.of("status", "SUBMITTED", "loanType", "PERSONAL");
        List<Map<String, Object>> docs = List.of(
            Map.of("status", "VERIFIED"),
            Map.of("status", "PENDING")
        );
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);
        when(documentClient.getDocuments(appId, token)).thenReturn(docs);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.approve(appId, adminId, token));
        
        assertEquals("Firstly verify the documents.", exception.getMessage());
        verify(decisionRepo, never()).save(any());
    }

    @Test
    void approve_WhenRequiredDocumentsMissing_ShouldThrowException() {
        // Arrange
        Map<String, Object> app = Map.of("status", "SUBMITTED", "loanType", "PERSONAL");
        List<Map<String, Object>> docs = List.of(
            Map.of("status", "VERIFIED"),
            Map.of("status", "VERIFIED")
        );
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);
        when(documentClient.getDocuments(appId, token)).thenReturn(docs);
        when(documentClient.validateDocuments(appId, "PERSONAL", token)).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.approve(appId, adminId, token));
        
        assertEquals("Firstly verify the documents.", exception.getMessage());
        verify(decisionRepo, never()).save(any());
    }

    // ===============================
    // ✅ REJECT APPLICATION TESTS
    // ===============================

    @Test
    void reject_WithValidRemarks_ShouldRejectSuccessfully() {
        // Arrange
        String remarks = "Incomplete documents";
        Map<String, Object> app = Map.of("status", "SUBMITTED");
        Map<String, String> rejectionResponse = Map.of("status", "REJECTED", "message", "Application rejected");
        
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);
        when(applicationClient.reject(appId, token)).thenReturn(rejectionResponse);
        when(decisionRepo.save(any(Decision.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Map<String, String> result = adminService.reject(appId, adminId, remarks, token);

        // Assert
        assertNotNull(result);
        assertEquals("REJECTED", result.get("status"));
        
        // Verify decision saved
        ArgumentCaptor<Decision> decisionCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepo, times(1)).save(decisionCaptor.capture());
        
        Decision savedDecision = decisionCaptor.getValue();
        assertEquals(appId, savedDecision.getApplicationId());
        assertEquals(adminId, savedDecision.getAdminId());
        assertEquals("REJECTED", savedDecision.getDecision());
        assertEquals(remarks, savedDecision.getRemarks());
        assertNotNull(savedDecision.getCreatedAt());
    }

    @Test
    void reject_WhenRemarksIsNull_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.reject(appId, adminId, null, token));
        
        assertEquals("Remarks are mandatory for rejection", exception.getMessage());
        verify(decisionRepo, never()).save(any());
        verify(applicationClient, never()).reject(any(), any());
    }

    @Test
    void reject_WhenRemarksIsBlank_ShouldThrowException() {
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.reject(appId, adminId, "   ", token));
        
        assertEquals("Remarks are mandatory for rejection", exception.getMessage());
        verify(decisionRepo, never()).save(any());
        verify(applicationClient, never()).reject(any(), any());
    }

    @Test
    void reject_WhenApplicationNotSubmitted_ShouldThrowException() {
        // Arrange
        Map<String, Object> app = Map.of("status", "DRAFT");
        when(applicationClient.getApplicationById(token, appId)).thenReturn(app);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminService.reject(appId, adminId, "Remarks", token));
        
        assertEquals("Application missing", exception.getMessage());
        verify(decisionRepo, never()).save(any());
        verify(applicationClient, never()).reject(any(), any());
    }

    // ===============================
    // 📊 GENERATE REPORT TESTS
    // ===============================

    @Test
    void generateReport_WithExistingReport_ShouldUpdateAndReturnReport() {
        // Arrange
        List<Map<String, Object>> apps = List.of(
            Map.of("status", "SUBMITTED"),
            Map.of("status", "APPROVED"),
            Map.of("status", "APPROVED"),
            Map.of("status", "REJECTED"),
            Map.of("status", "DRAFT") // Should be filtered out
        );
        
        Report existingReport = new Report();
        existingReport.setId(adminId);
        existingReport.setTotalApplications(0);
        
        when(applicationClient.getApplications(token)).thenReturn(apps);
        when(reportRepo.findById(adminId)).thenReturn(Optional.of(existingReport));
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Report result = adminService.generateReport(adminId, token);

        // Assert
        assertNotNull(result);
        assertEquals(adminId, result.getId());
        assertEquals(4, result.getTotalApplications()); // DRAFT excluded
        assertEquals(2, result.getApprovedCount());
        assertEquals(1, result.getRejectedCount());
        assertEquals(1, result.getPendingCount()); // SUBMITTED
        assertNotNull(result.getGeneratedAt());
        
        verify(reportRepo, times(1)).save(any(Report.class));
    }

    @Test
    void generateReport_WithNoExistingReport_ShouldCreateNewReport() {
        // Arrange
        List<Map<String, Object>> apps = List.of(
            Map.of("status", "SUBMITTED"),
            Map.of("status", "APPROVED")
        );
        
        when(applicationClient.getApplications(token)).thenReturn(apps);
        when(reportRepo.findById(adminId)).thenReturn(Optional.empty());
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Report result = adminService.generateReport(adminId, token);

        // Assert
        assertNotNull(result);
        assertEquals(adminId, result.getId());
        assertEquals(2, result.getTotalApplications());
        assertEquals(1, result.getApprovedCount());
        assertEquals(0, result.getRejectedCount());
        assertEquals(1, result.getPendingCount());
        
        verify(reportRepo, times(1)).save(any(Report.class));
    }

    @Test
    void generateReport_WhenNoApplications_ShouldReturnReportWithZeroCounts() {
        // Arrange
        when(applicationClient.getApplications(token)).thenReturn(List.of());
        when(reportRepo.findById(adminId)).thenReturn(Optional.empty());
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Report result = adminService.generateReport(adminId, token);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalApplications());
        assertEquals(0, result.getApprovedCount());
        assertEquals(0, result.getRejectedCount());
        assertEquals(0, result.getPendingCount());
    }

    @Test
    void generateReport_WhenApplicationsIsNull_ShouldHandleGracefully() {
        // Arrange
        when(applicationClient.getApplications(token)).thenReturn(null);
        when(reportRepo.findById(adminId)).thenReturn(Optional.empty());
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Report result = adminService.generateReport(adminId, token);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalApplications());
        assertEquals(0, result.getApprovedCount());
        assertEquals(0, result.getRejectedCount());
        assertEquals(0, result.getPendingCount());
    }

    @Test
    void generateReport_WithAllStatusTypes_ShouldCalculateCorrectly() {
        // Arrange
        List<Map<String, Object>> apps = List.of(
            Map.of("status", "SUBMITTED"),
            Map.of("status", "SUBMITTED"),
            Map.of("status", "APPROVED"),
            Map.of("status", "APPROVED"),
            Map.of("status", "APPROVED"),
            Map.of("status", "REJECTED"),
            Map.of("status", "REJECTED"),
            Map.of("status", "DRAFT"),
            Map.of("status", "draft")
        );
        
        when(applicationClient.getApplications(token)).thenReturn(apps);
        when(reportRepo.findById(adminId)).thenReturn(Optional.empty());
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Report result = adminService.generateReport(adminId, token);

        // Assert
        assertEquals(7, result.getTotalApplications()); // 2 drafts excluded
        assertEquals(3, result.getApprovedCount());
        assertEquals(2, result.getRejectedCount());
        assertEquals(2, result.getPendingCount());
    }
}