package com.finflow.document.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finflow.document.client.AuthClient;
import com.finflow.document.model.Document;
import com.finflow.document.model.LoanType;
import com.finflow.document.security.JwtUtil;
import com.finflow.document.service.DocumentService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false) // ✅ disable security
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService service;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuthClient authClient;

    @Autowired
    private ObjectMapper objectMapper;

    private final String token = "Bearer test-token";

    // ===============================
    // ✅ UPLOAD DOCUMENT
    // ===============================
    @Test
    void testUploadDocument() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "data".getBytes()
        );

        Mockito.when(jwtUtil.extractEmail("test-token"))
                .thenReturn("test@gmail.com");

        Mockito.when(authClient.getUserIdByEmail(Mockito.eq(token), Mockito.anyString()))
                .thenReturn(1L);

        Document doc = new Document();
        doc.setId(1L);

        Mockito.when(service.upload(Mockito.eq(token), Mockito.eq(1L), Mockito.eq(1L), Mockito.eq("PAN"), Mockito.any()))
                .thenReturn(doc);

        mockMvc.perform(multipart("/documents/upload-home")
                .file(file)
                .param("applicationId", "1")
                .param("documentType", "PAN")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    // ===============================
    // 🔄 REPLACE DOCUMENT
    // ===============================
    @Test
    void testReplaceDocument() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "new.pdf",
                "application/pdf",
                "data".getBytes()
        );

        Mockito.when(jwtUtil.extractEmail("test-token"))
                .thenReturn("test@gmail.com");

        Mockito.when(authClient.getUserIdByEmail(Mockito.eq(token), Mockito.anyString()))
                .thenReturn(1L);

        Document doc = new Document();
        doc.setId(1L);

        Mockito.when(service.replace(Mockito.eq(token), Mockito.eq(1L), Mockito.eq(1L), Mockito.any()))
                .thenReturn(doc);

        mockMvc.perform(multipart("/documents/1")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT"); // 🔥 important for multipart PUT
                    return request;
                })
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    // ===============================
    // 📄 GET DOCUMENTS
    // ===============================
    @Test
    void testGetByApplication() throws Exception {

        Mockito.when(service.getByApplication(1L))
                .thenReturn(List.of(new Document()));

        mockMvc.perform(get("/documents/application/1"))
                .andExpect(status().isOk());
    }

    // ===============================
    // ✅ VALIDATE DOCUMENTS
    // ===============================
    @Test
    void testValidateDocuments() throws Exception {

        Mockito.when(service.areDocumentsComplete(1L, LoanType.PERSONAL))
                .thenReturn(true);

        mockMvc.perform(get("/documents/validate/1/PERSONAL"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // ===============================
    // ✅ VERIFY DOCUMENT
    // ===============================
    @Test
    void testVerifyDocument() throws Exception {

        Document doc = new Document();
        doc.setId(1L);

        Mockito.when(service.verify(1L, token)).thenReturn(doc);

        mockMvc.perform(put("/documents/internal/1/verify")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }

    // ===============================
    // ❌ REJECT DOCUMENT
    // ===============================
    @Test
    void testRejectDocument() throws Exception {

        Document doc = new Document();
        doc.setId(1L);

        Mockito.when(service.reject(1L, "Invalid", token))
                .thenReturn(doc);

        mockMvc.perform(put("/documents/internal/1/reject")
                .param("remarks", "Invalid")
                .header("Authorization", token))
                .andExpect(status().isOk());
    }
}