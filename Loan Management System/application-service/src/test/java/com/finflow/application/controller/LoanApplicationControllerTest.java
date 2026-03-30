package com.finflow.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.application.dto.CreateApplicationRequest;
import com.finflow.application.dto.UpdateApplicationRequest;
import com.finflow.application.model.LoanApplication;
import com.finflow.application.model.LoanType;
import com.finflow.application.security.JwtUtil;
import com.finflow.application.service.LoanApplicationService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(LoanApplicationController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanApplicationService service;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private final String token = "Bearer test-token";

    // ===============================
    // ✅ CREATE APPLICATION
    // ===============================
    @Test
    void testCreateApplication() throws Exception {

        CreateApplicationRequest request = new CreateApplicationRequest();

        Mockito.when(jwtUtil.validateToken("test-token")).thenReturn(true);
        Mockito.when(jwtUtil.extractEmail("test-token")).thenReturn("test@gmail.com");
        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("USER");

        Mockito.when(restTemplate.exchange(
                Mockito.contains("/auth/user/email/"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(Long.class)
        )).thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        LoanApplication app = new LoanApplication();
        app.setId(1L);

        Mockito.when(service.create(Mockito.eq(1L), Mockito.any()))
                .thenReturn(app);

        mockMvc.perform(post("/applications")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ===============================
    // ✅ UPDATE APPLICATION
    // ===============================
    @Test
    void testUpdateApplication() throws Exception {

        UpdateApplicationRequest request = new UpdateApplicationRequest();

        Mockito.when(jwtUtil.validateToken("test-token")).thenReturn(true);
        Mockito.when(jwtUtil.extractEmail("test-token")).thenReturn("test@gmail.com");
        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("USER");

        Mockito.when(restTemplate.exchange(
                Mockito.contains("/auth/user/email/"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(Long.class)
        )).thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        LoanApplication app = new LoanApplication();
        app.setId(1L);

        Mockito.when(service.update(Mockito.eq(1L), Mockito.eq(1L), Mockito.any()))
                .thenReturn(app);

        mockMvc.perform(put("/applications/1")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ===============================
    // ✅ SUBMIT APPLICATION
    // ===============================
    @Test
    void testSubmitApplication() throws Exception {

        Mockito.when(jwtUtil.validateToken("test-token")).thenReturn(true);
        Mockito.when(jwtUtil.extractEmail("test-token")).thenReturn("test@gmail.com");
        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("USER");

        Mockito.when(restTemplate.exchange(
                Mockito.contains("/auth/user/email/"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(Long.class)
        )).thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanType(LoanType.HOME);  

        Mockito.when(service.getById(1L)).thenReturn(app);

        Mockito.when(restTemplate.exchange(
                Mockito.contains("/documents/validate/"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(Boolean.class)
        )).thenReturn(new ResponseEntity<>(true, HttpStatus.OK));

        Mockito.when(service.submit(1L, 1L)).thenReturn(app);

        mockMvc.perform(post("/applications/1/submit")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    // ===============================
    // ❌ SUBMIT APPLICATION - FAIL (DOCS INCOMPLETE)
    // ===============================
    @Test
    void testSubmitApplication_fail_docsIncomplete() throws Exception {

        Mockito.when(jwtUtil.validateToken("test-token")).thenReturn(true);
        Mockito.when(jwtUtil.extractEmail("test-token")).thenReturn("test@gmail.com");
        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("USER");

        LoanApplication app = new LoanApplication();
        app.setId(1L);
        app.setLoanType(LoanType.HOME);

        Mockito.when(service.getById(1L)).thenReturn(app);

        Mockito.when(restTemplate.exchange(
                Mockito.contains("/documents/validate/"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(Boolean.class)
        )).thenReturn(new ResponseEntity<>(false, HttpStatus.OK));

        mockMvc.perform(post("/applications/1/submit")
                .header("Authorization", token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please upload the documents first."));
    }

    // ===============================
    // ✅ GET MY APPLICATIONS
    // ===============================
    @Test
    void testGetMyApplications() throws Exception {

        Mockito.when(jwtUtil.validateToken("test-token")).thenReturn(true);
        Mockito.when(jwtUtil.extractEmail("test-token")).thenReturn("test@gmail.com");
        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("USER");

        Mockito.when(restTemplate.exchange(
                Mockito.contains("/auth/user/email/"),
                Mockito.eq(HttpMethod.GET),
                Mockito.any(),
                Mockito.eq(Long.class)
        )).thenReturn(new ResponseEntity<>(1L, HttpStatus.OK));

        Mockito.when(service.getByUser(1L))
                .thenReturn(List.of(new LoanApplication()));

        mockMvc.perform(get("/applications/my")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    // ===============================
    // ✅ GET STATUS
    // ===============================
    @Test
    void testGetStatus() throws Exception {

        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("ADMIN");
        Mockito.when(service.getStatus(1L)).thenReturn("APPROVED");

        mockMvc.perform(get("/applications/1/status")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(content().string("APPROVED"));
    }

    // ===============================
    // ✅ GET ALL (INTERNAL)
    // ===============================
    @Test
    void testGetAllApplications() throws Exception {

        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("ADMIN");
        Mockito.when(service.getAll())
                .thenReturn(List.of(new LoanApplication()));

        mockMvc.perform(get("/applications/internal/all")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    // ===============================
    // ✅ APPROVE (INTERNAL)
    // ===============================
    @Test
    void testApproveApplication() throws Exception {

        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("ADMIN");
        Mockito.when(service.approve(1L))
                .thenReturn(Map.of("message", "Approved"));

        mockMvc.perform(post("/applications/1/internal/approve")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Approved"));
    }

    // ===============================
    // ✅ REJECT (INTERNAL)
    // ===============================
    @Test
    void testRejectApplication() throws Exception {

        Mockito.when(jwtUtil.extractRole("test-token")).thenReturn("ADMIN");
        Mockito.when(service.reject(1L))
                .thenReturn(Map.of("message", "Rejected"));

        mockMvc.perform(post("/applications/1/internal/reject")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rejected"));
    }
}