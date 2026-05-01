package com.finflow.admin.controller;

import com.finflow.admin.client.AuthClient;
import com.finflow.admin.model.Report;
import com.finflow.admin.security.JwtUtil;
import com.finflow.admin.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthClient authClient;

    @BeforeEach
    void setUp() {
        when(jwtUtil.extractRole(anyString())).thenReturn("ADMIN");
        when(jwtUtil.extractEmail(anyString())).thenReturn("admin@test.com");
        when(authClient.getUserIdByEmail(anyString(), anyString())).thenReturn(1L);
    }

    @Test
    void getApplications_AdminRole_ReturnsApplications() throws Exception {
        Map<String, Object> app = new HashMap<>();
        app.put("id", 1L);
        app.put("status", "SUBMITTED");

        when(adminService.getApplications(anyString())).thenReturn(List.of(app));

        mockMvc.perform(get("/admin/applications")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getApplicationById_ValidId_ReturnsApplication() throws Exception {
        Map<String, Object> app = new HashMap<>();
        app.put("id", 1L);
        app.put("status", "SUBMITTED");

        when(adminService.getApplicationById(anyLong(), anyString())).thenReturn(app);

        mockMvc.perform(get("/admin/applications/1")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void approveApplication_ValidRequest_ReturnsSuccess() throws Exception {
        when(adminService.approve(anyLong(), anyLong(), anyString()))
                .thenReturn(Map.of("message", "Approved"));

        mockMvc.perform(post("/admin/applications/1/approve")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Approved"));
    }

    @Test
    void rejectApplication_ValidRequest_ReturnsSuccess() throws Exception {
        when(adminService.reject(anyLong(), anyLong(), anyString(), anyString()))
                .thenReturn(Map.of("message", "Rejected"));

        mockMvc.perform(post("/admin/applications/1/reject")
                        .param("remarks", "Invalid documents")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rejected"));
    }

    @Test
    void dashboard_ReturnsReport() throws Exception {
        Report report = new Report();
        report.setId(1L);
        report.setTotalApplications(10);
        report.setApprovedCount(5);
        report.setRejectedCount(2);
        report.setPendingCount(3);
        report.setGeneratedAt(LocalDateTime.now());

        when(adminService.generateReport(anyLong(), anyString())).thenReturn(report);

        mockMvc.perform(get("/admin/dashboard")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(10))
                .andExpect(jsonPath("$.approvedCount").value(5));
    }

    @Test
    void verifyDocument_ValidRequest_ReturnsSuccess() throws Exception {
        Map<String, Object> doc = Map.of("id", 1L, "status", "VERIFIED");
        when(adminService.verifyDocument(anyLong(), anyString())).thenReturn(doc);

        mockMvc.perform(put("/admin/documents/1/verify")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void rejectDocument_ValidRequest_ReturnsSuccess() throws Exception {
        Map<String, Object> doc = Map.of("id", 1L, "status", "REJECTED");
        when(adminService.rejectDocument(anyLong(), anyString(), anyString())).thenReturn(doc);

        mockMvc.perform(put("/admin/documents/1/reject")
                        .param("remarks", "Invalid")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }
}
