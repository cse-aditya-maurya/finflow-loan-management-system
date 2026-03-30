package com.finflow.admin.controller;

import com.finflow.admin.client.AuthClient;
import com.finflow.admin.model.Report;
import com.finflow.admin.service.AdminService;
import com.finflow.admin.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerUnitTest {

    @Mock
    private AdminService adminService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private AdminController adminController;

    private final String validToken = "valid.jwt.token";
    private final String authHeader = "Bearer " + validToken;
    private final Long appId = 1L;
    private final Long docId = 100L;
    private final Long adminId = 1000L;
    private final String adminEmail = "admin@example.com";

    @BeforeEach
    void setUp() {
        when(jwtUtil.extractRole(validToken)).thenReturn("ADMIN");
        when(jwtUtil.extractEmail(validToken)).thenReturn(adminEmail);
        when(authClient.getUserIdByEmail(authHeader, adminEmail)).thenReturn(adminId);
    }

    @Test
    void getApplications_ShouldReturnApplications() {
        List<Map<String, Object>> mockApps = List.of(Map.of("id", 1));
        when(adminService.getApplications(authHeader)).thenReturn(mockApps);

        Object result = adminController.getApplications(authHeader);

        assertNotNull(result);
        verify(adminService, times(1)).getApplications(authHeader);
    }

    @Test
    void getApplicationById_ShouldReturnApplication() {
        Map<String, Object> mockApp = Map.of("id", appId);
        when(adminService.getApplicationById(appId, authHeader)).thenReturn(mockApp);

        Object result = adminController.getApplicationById(authHeader, appId);

        assertNotNull(result);
        verify(adminService, times(1)).getApplicationById(appId, authHeader);
    }

    @Test
    void getDocuments_ShouldReturnDocuments() {
        List<Map<String, Object>> mockDocs = List.of(Map.of("id", 1));
        when(adminService.getDocuments(appId, authHeader)).thenReturn(mockDocs);

        Object result = adminController.getDocuments(authHeader, appId);

        assertNotNull(result);
        verify(adminService, times(1)).getDocuments(appId, authHeader);
    }

    @Test
    void verifyDocument_ShouldReturnVerificationResponse() {
        Map<String, Object> mockResponse = Map.of("status", "VERIFIED");
        when(adminService.verifyDocument(docId, authHeader)).thenReturn(mockResponse);

        Object result = adminController.verifyDocument(authHeader, docId);

        assertNotNull(result);
        verify(adminService, times(1)).verifyDocument(docId, authHeader);
    }

    @Test
    void rejectDocument_ShouldReturnRejectionResponse() {
        String remarks = "Invalid document";
        Map<String, Object> mockResponse = Map.of("status", "REJECTED");
        when(adminService.rejectDocument(docId, remarks, authHeader)).thenReturn(mockResponse);

        Object result = adminController.rejectDocument(authHeader, docId, remarks);

        assertNotNull(result);
        verify(adminService, times(1)).rejectDocument(docId, remarks, authHeader);
    }

    @Test
    void approveApplication_ShouldReturnApprovalResponse() {
        Map<String, String> mockResponse = Map.of("status", "APPROVED");
        when(adminService.approve(appId, adminId, authHeader)).thenReturn(mockResponse);

        Object result = adminController.approveApplication(authHeader, appId);

        assertNotNull(result);
        verify(adminService, times(1)).approve(appId, adminId, authHeader);
        verify(authClient, times(1)).getUserIdByEmail(authHeader, adminEmail);
    }

    @Test
    void rejectApplication_ShouldReturnRejectionResponse() {
        String remarks = "Incomplete";
        Map<String, String> mockResponse = Map.of("status", "REJECTED");
        when(adminService.reject(appId, adminId, remarks, authHeader)).thenReturn(mockResponse);

        Object result = adminController.rejectApplication(authHeader, appId, remarks);

        assertNotNull(result);
        verify(adminService, times(1)).reject(appId, adminId, remarks, authHeader);
    }

    @Test
    void dashboard_ShouldReturnReport() {
        // Create actual Report object, not Map
        Report mockReport = new Report();
        mockReport.setId(adminId);
        mockReport.setTotalApplications(10);
        mockReport.setApprovedCount(5);
        mockReport.setRejectedCount(3);
        mockReport.setPendingCount(2);
        mockReport.setGeneratedAt(LocalDateTime.now());
        
        when(adminService.generateReport(adminId, authHeader)).thenReturn(mockReport);

        Object result = adminController.dashboard(authHeader);

        assertNotNull(result);
        assertTrue(result instanceof Report);
        Report report = (Report) result;
        assertEquals(10, report.getTotalApplications());
        assertEquals(5, report.getApprovedCount());
        
        verify(adminService, times(1)).generateReport(adminId, authHeader);
    }

    @Test
    void getApplications_WithNonAdmin_ShouldThrowException() {
        when(jwtUtil.extractRole(validToken)).thenReturn("USER");

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> adminController.getApplications(authHeader));
        
        assertEquals("Access Denied: Admin only", exception.getMessage());
        verify(adminService, never()).getApplications(any());
    }
}