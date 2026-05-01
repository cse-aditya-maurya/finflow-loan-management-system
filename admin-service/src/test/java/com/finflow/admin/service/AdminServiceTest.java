package com.finflow.admin.service;

import com.finflow.admin.client.ApplicationClient;
import com.finflow.admin.client.DocumentClient;
import com.finflow.admin.model.Decision;
import com.finflow.admin.model.Report;
import com.finflow.admin.repository.DecisionRepository;
import com.finflow.admin.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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

    private Map<String, Object> testApplication;
    private List<Map<String, Object>> testDocuments;

    @BeforeEach
    void setUp() {
        testApplication = new HashMap<>();
        testApplication.put("id", 1L);
        testApplication.put("userId", 1L);
        testApplication.put("status", "SUBMITTED");
        testApplication.put("loanType", "PERSONAL");

        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("id", 1L);
        doc1.put("status", "VERIFIED");
        doc1.put("documentType", "AADHAAR_CARD");

        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", 2L);
        doc2.put("status", "VERIFIED");
        doc2.put("documentType", "PAN_CARD");

        testDocuments = Arrays.asList(doc1, doc2);
    }

    @Test
    void getApplications_FiltersDraftApplications() {
        Map<String, Object> draftApp = new HashMap<>();
        draftApp.put("status", "DRAFT");

        List<Map<String, Object>> apps = Arrays.asList(testApplication, draftApp);
        when(applicationClient.getApplications(anyString())).thenReturn(apps);

        List<Map<String, Object>> result = adminService.getApplications("Bearer token");

        assertEquals(1, result.size());
        assertEquals("SUBMITTED", result.get(0).get("status"));
    }

    @Test
    void getApplicationById_ValidApplication_ReturnsApplication() {
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);

        Map<String, Object> result = adminService.getApplicationById(1L, "Bearer token");

        assertNotNull(result);
        assertEquals(1L, result.get("id"));
    }

    @Test
    void getApplicationById_DraftApplication_ThrowsException() {
        testApplication.put("status", "DRAFT");
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);

        assertThrows(RuntimeException.class,
                () -> adminService.getApplicationById(1L, "Bearer token"));
    }

    @Test
    void approve_ValidApplication_Success() {
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);
        when(documentClient.getDocuments(anyLong(), anyString())).thenReturn(testDocuments);
        when(documentClient.validateDocuments(anyLong(), anyString(), anyString())).thenReturn(true);
        when(applicationClient.approve(anyLong(), anyString())).thenReturn(Map.of("message", "Approved"));
        when(decisionRepo.save(any(Decision.class))).thenReturn(new Decision());

        Map<String, String> result = adminService.approve(1L, 1L, "Bearer token");

        assertNotNull(result);
        verify(decisionRepo).save(any(Decision.class));
        verify(applicationClient).approve(1L, "Bearer token");
    }

    @Test
    void approve_NonSubmittedApplication_ThrowsException() {
        testApplication.put("status", "DRAFT");
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);

        assertThrows(RuntimeException.class,
                () -> adminService.approve(1L, 1L, "Bearer token"));
    }

    @Test
    void approve_NoDocuments_ThrowsException() {
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);
        when(documentClient.getDocuments(anyLong(), anyString())).thenReturn(Collections.emptyList());

        assertThrows(RuntimeException.class,
                () -> adminService.approve(1L, 1L, "Bearer token"));
    }

    @Test
    void approve_UnverifiedDocuments_ThrowsException() {
        testDocuments.get(0).put("status", "UPLOADED");
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);
        when(documentClient.getDocuments(anyLong(), anyString())).thenReturn(testDocuments);

        assertThrows(RuntimeException.class,
                () -> adminService.approve(1L, 1L, "Bearer token"));
    }

    @Test
    void approve_IncompleteDocuments_ThrowsException() {
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);
        when(documentClient.getDocuments(anyLong(), anyString())).thenReturn(testDocuments);
        when(documentClient.validateDocuments(anyLong(), anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> adminService.approve(1L, 1L, "Bearer token"));
    }

    @Test
    void reject_ValidApplication_Success() {
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);
        when(applicationClient.reject(anyLong(), anyString())).thenReturn(Map.of("message", "Rejected"));
        when(decisionRepo.save(any(Decision.class))).thenReturn(new Decision());

        Map<String, String> result = adminService.reject(1L, 1L, "Invalid documents", "Bearer token");

        assertNotNull(result);
        verify(decisionRepo).save(any(Decision.class));
        verify(applicationClient).reject(1L, "Bearer token");
    }

    @Test
    void reject_EmptyRemarks_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> adminService.reject(1L, 1L, "", "Bearer token"));
    }

    @Test
    void reject_NullRemarks_ThrowsException() {
        assertThrows(RuntimeException.class,
                () -> adminService.reject(1L, 1L, null, "Bearer token"));
    }

    @Test
    void reject_NonSubmittedApplication_ThrowsException() {
        testApplication.put("status", "APPROVED");
        when(applicationClient.getApplicationById(anyString(), anyLong())).thenReturn(testApplication);

        assertThrows(RuntimeException.class,
                () -> adminService.reject(1L, 1L, "Invalid", "Bearer token"));
    }

    @Test
    void generateReport_Success() {
        Map<String, Object> app1 = new HashMap<>(testApplication);
        app1.put("status", "APPROVED");

        Map<String, Object> app2 = new HashMap<>(testApplication);
        app2.put("status", "REJECTED");

        Map<String, Object> app3 = new HashMap<>(testApplication);
        app3.put("status", "SUBMITTED");

        List<Map<String, Object>> apps = Arrays.asList(app1, app2, app3);
        when(applicationClient.getApplications(anyString())).thenReturn(apps);
        when(reportRepo.findById(anyLong())).thenReturn(Optional.of(new Report()));
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Report result = adminService.generateReport(1L, "Bearer token");

        assertNotNull(result);
        assertEquals(3, result.getTotalApplications());
        assertEquals(1, result.getApprovedCount());
        assertEquals(1, result.getRejectedCount());
        assertEquals(1, result.getPendingCount());
    }

    @Test
    void getDocuments_ReturnsDocuments() {
        when(documentClient.getDocuments(anyLong(), anyString())).thenReturn(testDocuments);

        List<Map<String, Object>> result = adminService.getDocuments(1L, "Bearer token");

        assertEquals(2, result.size());
        verify(documentClient).getDocuments(1L, "Bearer token");
    }

    @Test
    void viewDocument_ReturnsDocument() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", 1L);
        when(documentClient.viewDocument(anyLong(), anyString())).thenReturn(doc);

        Object result = adminService.viewDocument(1L, "Bearer token");

        assertNotNull(result);
        verify(documentClient).viewDocument(1L, "Bearer token");
    }

    @Test
    void verifyDocument_Success() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("status", "VERIFIED");
        when(documentClient.verify(anyLong(), anyString())).thenReturn(doc);

        Object result = adminService.verifyDocument(1L, "Bearer token");

        assertNotNull(result);
        verify(documentClient).verify(1L, "Bearer token");
    }

    @Test
    void rejectDocument_Success() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("status", "REJECTED");
        when(documentClient.reject(anyLong(), anyString(), anyString())).thenReturn(doc);

        Object result = adminService.rejectDocument(1L, "Invalid", "Bearer token");

        assertNotNull(result);
        verify(documentClient).reject(1L, "Invalid", "Bearer token");
    }
}
