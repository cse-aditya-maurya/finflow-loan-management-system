package com.finflow.document.controller;

import com.finflow.document.client.AuthClient;
import com.finflow.document.enums.PersonalLoanDocument;
import com.finflow.document.model.Document;
import com.finflow.document.model.DocumentStatus;
import com.finflow.document.model.LoanType;
import com.finflow.document.security.JwtUtil;
import com.finflow.document.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthClient authClient;

    private Document testDocument;

    @BeforeEach
    void setUp() {
        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setUserId(1L);
        testDocument.setApplicationId(1L);
        testDocument.setDocumentType("AADHAAR_CARD");
        testDocument.setStatus(DocumentStatus.UPLOADED);
        testDocument.setUploadedAt(LocalDateTime.now());
    }

    @Test
    void uploadPersonal_ValidRequest_ReturnsDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(jwtUtil.extractEmail(anyString())).thenReturn("user@test.com");
        when(jwtUtil.extractRole(anyString())).thenReturn("USER");
        when(authClient.getUserIdByEmail(anyString(), anyString())).thenReturn(1L);
        when(documentService.upload(anyString(), anyLong(), anyLong(), anyString(), any())).thenReturn(testDocument);

        mockMvc.perform(multipart("/documents/upload/personal")
                        .file(file)
                        .param("applicationId", "1")
                        .param("documentType", PersonalLoanDocument.AADHAR.name())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.documentType").value("AADHAAR_CARD"));
    }

    @Test
    void replace_ValidRequest_ReturnsDocument() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        when(jwtUtil.extractEmail(anyString())).thenReturn("user@test.com");
        when(authClient.getUserIdByEmail(anyString(), anyString())).thenReturn(1L);
        when(documentService.replace(anyString(), anyLong(), anyLong(), any())).thenReturn(testDocument);

        mockMvc.perform(multipart("/documents/1")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getByApplication_ReturnsDocumentList() throws Exception {
        List<Document> documents = Arrays.asList(testDocument);
        when(documentService.getByApplication(anyLong())).thenReturn(documents);

        mockMvc.perform(get("/documents/application/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void viewDocument_ValidId_ReturnsDocument() throws Exception {
        when(documentService.getDocumentById(anyLong())).thenReturn(testDocument);

        mockMvc.perform(get("/documents/1/view")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void validateDocuments_ReturnsBoolean() throws Exception {
        when(documentService.areDocumentsComplete(anyLong(), any(LoanType.class))).thenReturn(true);

        mockMvc.perform(get("/documents/validate/1/PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void verify_ValidRequest_ReturnsDocument() throws Exception {
        testDocument.setStatus(DocumentStatus.VERIFIED);
        when(documentService.verify(anyLong(), anyString())).thenReturn(testDocument);

        mockMvc.perform(put("/documents/internal/1/verify")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"));
    }

    @Test
    void reject_ValidRequest_ReturnsDocument() throws Exception {
        testDocument.setStatus(DocumentStatus.REJECTED);
        testDocument.setRemarks("Invalid document");
        when(documentService.reject(anyLong(), anyString(), anyString())).thenReturn(testDocument);

        mockMvc.perform(put("/documents/internal/1/reject")
                        .param("remarks", "Invalid document")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.remarks").value("Invalid document"));
    }
}
