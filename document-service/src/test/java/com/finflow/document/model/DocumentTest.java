package com.finflow.document.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DocumentTest {

    @Test
    void createDocument_AllFields_Success() {
        Document document = new Document();
        document.setId(1L);
        document.setUserId(1L);
        document.setApplicationId(1L);
        document.setDocumentType("AADHAAR_CARD");
        document.setFileUrl("/uploads/test.pdf");
        document.setStatus(DocumentStatus.UPLOADED);
        document.setUploadedAt(LocalDateTime.now());

        assertEquals(1L, document.getId());
        assertEquals(1L, document.getUserId());
        assertEquals(1L, document.getApplicationId());
        assertEquals("AADHAAR_CARD", document.getDocumentType());
        assertEquals("/uploads/test.pdf", document.getFileUrl());
        assertEquals(DocumentStatus.UPLOADED, document.getStatus());
        assertNotNull(document.getUploadedAt());
    }

    @Test
    void setStatus_VerifiedStatus_Success() {
        Document document = new Document();
        document.setStatus(DocumentStatus.VERIFIED);

        assertEquals(DocumentStatus.VERIFIED, document.getStatus());
    }

    @Test
    void setRemarks_Success() {
        Document document = new Document();
        document.setRemarks("Document is invalid");

        assertEquals("Document is invalid", document.getRemarks());
    }
}
