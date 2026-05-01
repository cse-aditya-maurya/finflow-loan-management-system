package com.finflow.document.service;

import com.finflow.document.client.ApplicationClient;
import com.finflow.document.dto.LoanApplicationDTO;
import com.finflow.document.model.Document;
import com.finflow.document.model.DocumentStatus;
import com.finflow.document.model.LoanType;
import com.finflow.document.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository repository;

    @Mock
    private ApplicationClient applicationClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    private Document testDocument;
    private LoanApplicationDTO testApplication;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        testDocument = new Document();
        testDocument.setId(1L);
        testDocument.setUserId(1L);
        testDocument.setApplicationId(1L);
        testDocument.setDocumentType("AADHAAR_CARD");
        testDocument.setStatus(DocumentStatus.UPLOADED);
        testDocument.setUploadedAt(LocalDateTime.now());

        testApplication = new LoanApplicationDTO();
        testApplication.setId(1L);
        testApplication.setUserId(1L);
        testApplication.setStatus("SUBMITTED");

        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
    }

    @Test
    void upload_ValidDocument_Success() throws Exception {
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);
        when(repository.existsByApplicationIdAndDocumentType(anyLong(), anyString())).thenReturn(false);
        when(repository.save(any(Document.class))).thenReturn(testDocument);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        Document result = documentService.upload("Bearer token", 1L, 1L, "AADHAAR_CARD", testFile);

        assertNotNull(result);
        verify(repository).save(any(Document.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void upload_UnauthorizedUser_ThrowsException() {
        testApplication.setUserId(2L);
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);

        assertThrows(RuntimeException.class,
                () -> documentService.upload("Bearer token", 1L, 1L, "AADHAAR_CARD", testFile));
    }

    @Test
    void upload_EmptyFile_ThrowsException() {
        MultipartFile emptyFile = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

        assertThrows(RuntimeException.class,
                () -> documentService.upload("Bearer token", 1L, 1L, "AADHAAR_CARD", emptyFile));
    }

    @Test
    void upload_DuplicateDocument_ThrowsException() {
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);
        when(repository.existsByApplicationIdAndDocumentType(anyLong(), anyString())).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> documentService.upload("Bearer token", 1L, 1L, "AADHAAR_CARD", testFile));
    }

    @Test
    void replace_ValidDocument_Success() throws Exception {
        testDocument.setFileUrl(tempDir.resolve("old.pdf").toString());
        when(repository.findById(anyLong())).thenReturn(Optional.of(testDocument));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);
        when(repository.save(any(Document.class))).thenReturn(testDocument);

        Document result = documentService.replace("Bearer token", 1L, 1L, testFile);

        assertNotNull(result);
        verify(repository).save(any(Document.class));
    }

    @Test
    void replace_DocumentNotFound_ThrowsException() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> documentService.replace("Bearer token", 1L, 1L, testFile));
    }

    @Test
    void replace_UnauthorizedUser_ThrowsException() {
        testDocument.setUserId(2L);
        when(repository.findById(anyLong())).thenReturn(Optional.of(testDocument));

        assertThrows(RuntimeException.class,
                () -> documentService.replace("Bearer token", 1L, 1L, testFile));
    }

    @Test
    void getByApplication_ReturnsDocuments() {
        List<Document> documents = Arrays.asList(testDocument);
        when(repository.findByApplicationId(anyLong())).thenReturn(documents);

        List<Document> result = documentService.getByApplication(1L);

        assertEquals(1, result.size());
        verify(repository).findByApplicationId(1L);
    }

    @Test
    void getDocumentById_ValidId_ReturnsDocument() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(testDocument));

        Document result = documentService.getDocumentById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void getDocumentById_InvalidId_ThrowsException() {
        when(repository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> documentService.getDocumentById(1L));
    }

    @Test
    void areDocumentsComplete_AllDocumentsPresent_ReturnsTrue() {
        Document doc1 = new Document();
        doc1.setDocumentType("AADHAR");
        doc1.setStatus(DocumentStatus.UPLOADED);

        Document doc2 = new Document();
        doc2.setDocumentType("PAN");
        doc2.setStatus(DocumentStatus.VERIFIED);

        Document doc3 = new Document();
        doc3.setDocumentType("INCOME_PROOF");
        doc3.setStatus(DocumentStatus.VERIFIED);

        when(repository.findByApplicationId(anyLong())).thenReturn(Arrays.asList(doc1, doc2, doc3));

        boolean result = documentService.areDocumentsComplete(1L, LoanType.PERSONAL);

        assertTrue(result);
    }

    @Test
    void areDocumentsComplete_MissingDocuments_ReturnsFalse() {
        Document doc1 = new Document();
        doc1.setDocumentType("AADHAR");
        doc1.setStatus(DocumentStatus.UPLOADED);

        when(repository.findByApplicationId(anyLong())).thenReturn(Arrays.asList(doc1));

        boolean result = documentService.areDocumentsComplete(1L, LoanType.PERSONAL);

        assertFalse(result);
    }

    @Test
    void verify_ValidDocument_Success() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(testDocument));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);
        when(repository.save(any(Document.class))).thenReturn(testDocument);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        Document result = documentService.verify(1L, "Bearer token");

        assertNotNull(result);
        assertEquals(DocumentStatus.VERIFIED, result.getStatus());
        verify(repository).save(any(Document.class));
    }

    @Test
    void verify_DraftApplication_ThrowsException() {
        testApplication.setStatus("DRAFT");
        when(repository.findById(anyLong())).thenReturn(Optional.of(testDocument));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);

        assertThrows(RuntimeException.class,
                () -> documentService.verify(1L, "Bearer token"));
    }

    @Test
    void reject_ValidDocument_Success() {
        when(repository.findById(anyLong())).thenReturn(Optional.of(testDocument));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);
        when(repository.save(any(Document.class))).thenReturn(testDocument);
        doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        Document result = documentService.reject(1L, "Invalid document", "Bearer token");

        assertNotNull(result);
        assertEquals(DocumentStatus.REJECTED, result.getStatus());
        assertEquals("Invalid document", result.getRemarks());
        verify(repository).save(any(Document.class));
    }

    @Test
    void reject_DraftApplication_ThrowsException() {
        testApplication.setStatus("DRAFT");
        when(repository.findById(anyLong())).thenReturn(Optional.of(testDocument));
        when(applicationClient.getById(anyString(), anyLong())).thenReturn(testApplication);

        assertThrows(RuntimeException.class,
                () -> documentService.reject(1L, "Invalid", "Bearer token"));
    }
}
