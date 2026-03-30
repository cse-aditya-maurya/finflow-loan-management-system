package com.finflow.document.service;

import com.finflow.document.client.ApplicationClient;
import com.finflow.document.dto.LoanApplicationDTO;
import com.finflow.document.model.*;
import com.finflow.document.repository.DocumentRepository;
import com.finflow.document.util.LoanDocumentRequirements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ApplicationClient applicationClient;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private DocumentService documentService;

    @TempDir
    Path tempDir;

    private static final String TEST_TOKEN = "Bearer test-token";
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_APPLICATION_ID = 100L;
    private static final Long TEST_DOCUMENT_ID = 1000L;
    private static final String TEST_DOCUMENT_TYPE = "AADHAR";

    private LoanApplicationDTO mockApplication;
    private Document mockDocument;

    @BeforeEach
    void setUp() {
        // Set upload directory to temp directory
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());

        // Setup mock application - using constructor or builder pattern
        mockApplication = new LoanApplicationDTO();
        // Assuming your DTO has these methods - adjust based on actual DTO
        mockApplication.setUserId(TEST_USER_ID);
        mockApplication.setStatus("SUBMITTED");
        // If there's no setId method, don't set it
        // If your DTO has a different way to set application ID, use that
        // For example, some DTOs might have setApplicationId() or just id field

        // Setup mock document
        mockDocument = new Document();
        mockDocument.setId(TEST_DOCUMENT_ID);
        mockDocument.setUserId(TEST_USER_ID);
        mockDocument.setApplicationId(TEST_APPLICATION_ID);
        mockDocument.setDocumentType(TEST_DOCUMENT_TYPE);
        mockDocument.setStatus(DocumentStatus.UPLOADED);
        mockDocument.setUploadedAt(LocalDateTime.now());
        mockDocument.setFileUrl(tempDir.resolve("old-file.txt").toString());
    }

    // =========================
    // ✅ UPLOAD DOCUMENT TESTS
    // =========================

    @Test
    void upload_Success() throws Exception {
        // Arrange
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getBytes()).thenReturn("test content".getBytes());
        when(documentRepository.existsByApplicationIdAndDocumentType(TEST_APPLICATION_ID, TEST_DOCUMENT_TYPE))
                .thenReturn(false);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document result = documentService.upload(TEST_TOKEN, TEST_USER_ID, TEST_APPLICATION_ID, 
                TEST_DOCUMENT_TYPE, multipartFile);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_USER_ID, result.getUserId());
        assertEquals(TEST_APPLICATION_ID, result.getApplicationId());
        assertEquals(TEST_DOCUMENT_TYPE, result.getDocumentType());
        assertEquals(DocumentStatus.UPLOADED, result.getStatus());
        assertTrue(result.getFileUrl().contains(".pdf"));
        
        verify(documentRepository).save(any(Document.class));
        verify(documentRepository).existsByApplicationIdAndDocumentType(TEST_APPLICATION_ID, TEST_DOCUMENT_TYPE);
    }

    @Test
    void upload_Unauthorized_UserMismatch_ThrowsException() throws Exception {
        // Arrange
        LoanApplicationDTO differentUserApp = new LoanApplicationDTO();
        differentUserApp.setUserId(999L);
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(differentUserApp);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.upload(TEST_TOKEN, TEST_USER_ID, TEST_APPLICATION_ID, TEST_DOCUMENT_TYPE, multipartFile)
        );
        
        assertEquals("Unauthorized: You do not own this application", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void upload_NullApplication_ThrowsException() throws Exception {
        // Arrange
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.upload(TEST_TOKEN, TEST_USER_ID, TEST_APPLICATION_ID, TEST_DOCUMENT_TYPE, multipartFile)
        );
        
        assertEquals("Unauthorized: You do not own this application", exception.getMessage());
    }

    @Test
    void upload_EmptyFile_ThrowsException() throws Exception {
        // Arrange
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.upload(TEST_TOKEN, TEST_USER_ID, TEST_APPLICATION_ID, TEST_DOCUMENT_TYPE, multipartFile)
        );
        
        assertEquals("File is missing", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    @Test
    void upload_DuplicateDocument_ThrowsException() throws Exception {
        // Arrange
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);
        when(documentRepository.existsByApplicationIdAndDocumentType(TEST_APPLICATION_ID, TEST_DOCUMENT_TYPE))
                .thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.upload(TEST_TOKEN, TEST_USER_ID, TEST_APPLICATION_ID, TEST_DOCUMENT_TYPE, multipartFile)
        );
        
        assertEquals("Document already uploaded", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    // =========================
    // 🔄 REPLACE DOCUMENT TESTS
    // =========================

    @Test
    void replace_Success() throws Exception {
        // Arrange
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.of(mockDocument));
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getOriginalFilename()).thenReturn("new-file.pdf");
        when(multipartFile.getBytes()).thenReturn("new content".getBytes());
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Create old file
        Path oldFile = Path.of(mockDocument.getFileUrl());
        Files.createDirectories(oldFile.getParent());
        Files.write(oldFile, "old content".getBytes());

        // Act
        Document result = documentService.replace(TEST_TOKEN, TEST_USER_ID, TEST_DOCUMENT_ID, multipartFile);

        // Assert
        assertNotNull(result);
        assertEquals(DocumentStatus.UPLOADED, result.getStatus());
        assertNotNull(result.getUpdatedAt());
        assertFalse(Files.exists(oldFile)); // Old file should be deleted
        
        verify(documentRepository).save(any(Document.class));
    }

    @Test
    void replace_DocumentNotFound_ThrowsException() throws Exception {
        // Arrange
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.replace(TEST_TOKEN, TEST_USER_ID, TEST_DOCUMENT_ID, multipartFile)
        );
        
        assertEquals("Document not found", exception.getMessage());
    }

    @Test
    void replace_Unauthorized_UserMismatch_ThrowsException() throws Exception {
        // Arrange
        mockDocument.setUserId(999L);
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.of(mockDocument));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.replace(TEST_TOKEN, TEST_USER_ID, TEST_DOCUMENT_ID, multipartFile)
        );
        
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void replace_ApplicationOwnershipMismatch_ThrowsException() throws Exception {
        // Arrange
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.of(mockDocument));
        LoanApplicationDTO differentUserApp = new LoanApplicationDTO();
        differentUserApp.setUserId(999L);
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(differentUserApp);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.replace(TEST_TOKEN, TEST_USER_ID, TEST_DOCUMENT_ID, multipartFile)
        );
        
        assertEquals("Unauthorized: Application ownership mismatch", exception.getMessage());
    }

    @Test
    void replace_EmptyFile_ThrowsException() throws Exception {
        // Arrange
        when(multipartFile.isEmpty()).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.replace(TEST_TOKEN, TEST_USER_ID, TEST_DOCUMENT_ID, multipartFile)
        );
        
        assertEquals("File is missing", exception.getMessage());
    }

    // =========================
    // 📄 GET DOCUMENTS TESTS
    // =========================

    @Test
    void getByApplication_ReturnsDocumentList() {
        // Arrange
        List<Document> expectedDocuments = Arrays.asList(mockDocument, new Document());
        when(documentRepository.findByApplicationId(TEST_APPLICATION_ID)).thenReturn(expectedDocuments);

        // Act
        List<Document> result = documentService.getByApplication(TEST_APPLICATION_ID);

        // Assert
        assertEquals(expectedDocuments.size(), result.size());
        verify(documentRepository).findByApplicationId(TEST_APPLICATION_ID);
    }

    @Test
    void getByApplication_NoDocuments_ReturnsEmptyList() {
        // Arrange
        when(documentRepository.findByApplicationId(TEST_APPLICATION_ID)).thenReturn(List.of());

        // Act
        List<Document> result = documentService.getByApplication(TEST_APPLICATION_ID);

        // Assert
        assertTrue(result.isEmpty());
        verify(documentRepository).findByApplicationId(TEST_APPLICATION_ID);
    }

    // =========================
    // 🔥 VALIDATE DOCUMENTS TESTS
    // =========================

    @Test
    void areDocumentsComplete_AllRequiredDocumentsPresent_ReturnsTrue() {
        // Arrange
        LoanType loanType = LoanType.PERSONAL;
        
        List<Document> uploadedDocs = Arrays.asList(
            createDocumentWithStatus("AADHAR", DocumentStatus.UPLOADED),
            createDocumentWithStatus("PAN", DocumentStatus.VERIFIED),
            createDocumentWithStatus("SALARY_SLIP", DocumentStatus.UPLOADED)
        );
        
        when(documentRepository.findByApplicationId(TEST_APPLICATION_ID)).thenReturn(uploadedDocs);
        
        // Mock the static method
        try (var mockedStatic = mockStatic(LoanDocumentRequirements.class)) {
            // Assuming getRequiredDocs returns Set<DocumentType> enum
            mockedStatic.when(() -> LoanDocumentRequirements.getRequiredDocs(loanType))
                        .thenReturn(Set.of(DocumentType.AADHAR, DocumentType.PAN, DocumentType.SALARY_SLIP));

            // Act
            boolean result = documentService.areDocumentsComplete(TEST_APPLICATION_ID, loanType);

            // Assert
            assertTrue(result);
        }
    }

    @Test
    void areDocumentsComplete_MissingRequiredDocuments_ReturnsFalse() {
        // Arrange
        LoanType loanType = LoanType.PERSONAL;
        
        List<Document> uploadedDocs = Arrays.asList(
            createDocumentWithStatus("AADHAR", DocumentStatus.UPLOADED),
            createDocumentWithStatus("PAN", DocumentStatus.VERIFIED)
        );
        
        when(documentRepository.findByApplicationId(TEST_APPLICATION_ID)).thenReturn(uploadedDocs);
        
        try (var mockedStatic = mockStatic(LoanDocumentRequirements.class)) {
            mockedStatic.when(() -> LoanDocumentRequirements.getRequiredDocs(loanType))
                        .thenReturn(Set.of(DocumentType.AADHAR, DocumentType.PAN, DocumentType.SALARY_SLIP));

            // Act
            boolean result = documentService.areDocumentsComplete(TEST_APPLICATION_ID, loanType);

            // Assert
            assertFalse(result);
        }
    }

    @Test
    void areDocumentsComplete_OnlyConsidersUploadedAndVerifiedStatus() {
        // Arrange
        LoanType loanType = LoanType.HOME;
        
        List<Document> uploadedDocs = Arrays.asList(
            createDocumentWithStatus("AADHAR", DocumentStatus.UPLOADED),
            createDocumentWithStatus("PAN", DocumentStatus.REJECTED),
            createDocumentWithStatus("PROPERTY", DocumentStatus.UPLOADED)
        );
        
        when(documentRepository.findByApplicationId(TEST_APPLICATION_ID)).thenReturn(uploadedDocs);
        
        try (var mockedStatic = mockStatic(LoanDocumentRequirements.class)) {
            mockedStatic.when(() -> LoanDocumentRequirements.getRequiredDocs(loanType))
                        .thenReturn(Set.of(DocumentType.AADHAR, DocumentType.PAN));

            // Act
            boolean result = documentService.areDocumentsComplete(TEST_APPLICATION_ID, loanType);

            // Assert
            assertFalse(result); // PAN is REJECTED, not counted
        }
    }

    // =========================
    // ✅ VERIFY DOCUMENT TESTS
    // =========================

    @Test
    void verify_Success() {
        // Arrange
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.of(mockDocument));
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document result = documentService.verify(TEST_DOCUMENT_ID, TEST_TOKEN);

        // Assert
        assertEquals(DocumentStatus.VERIFIED, result.getStatus());
        verify(documentRepository).save(mockDocument);
    }

    @Test
    void verify_DocumentNotFound_ThrowsException() {
        // Arrange
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.verify(TEST_DOCUMENT_ID, TEST_TOKEN)
        );
        
        assertEquals("Document not found", exception.getMessage());
    }

    @Test
    void verify_ApplicationInDraftStatus_ThrowsException() {
        // Arrange
        mockApplication.setStatus("DRAFT");
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.of(mockDocument));
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.verify(TEST_DOCUMENT_ID, TEST_TOKEN)
        );
        
        assertEquals("Cannot verify documents before submission", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    // =========================
    // ❌ REJECT DOCUMENT TESTS
    // =========================

    @Test
    void reject_Success() {
        // Arrange
        String remarks = "Document is blurry";
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.of(mockDocument));
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);
        when(documentRepository.save(any(Document.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Document result = documentService.reject(TEST_DOCUMENT_ID, remarks, TEST_TOKEN);

        // Assert
        assertEquals(DocumentStatus.REJECTED, result.getStatus());
        assertEquals(remarks, result.getRemarks());
        verify(documentRepository).save(mockDocument);
    }

    @Test
    void reject_DocumentNotFound_ThrowsException() {
        // Arrange
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.reject(TEST_DOCUMENT_ID, "Remarks", TEST_TOKEN)
        );
        
        assertEquals("Document not found", exception.getMessage());
    }

    @Test
    void reject_ApplicationInDraftStatus_ThrowsException() {
        // Arrange
        mockApplication.setStatus("DRAFT");
        when(documentRepository.findById(TEST_DOCUMENT_ID)).thenReturn(Optional.of(mockDocument));
        when(applicationClient.getById(TEST_TOKEN, TEST_APPLICATION_ID)).thenReturn(mockApplication);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            documentService.reject(TEST_DOCUMENT_ID, "Remarks", TEST_TOKEN)
        );
        
        assertEquals("Cannot reject before submission", exception.getMessage());
        verify(documentRepository, never()).save(any(Document.class));
    }

    // =========================
    // HELPER METHODS
    // =========================

    private Document createDocumentWithStatus(String documentType, DocumentStatus status) {
        Document doc = new Document();
        doc.setDocumentType(documentType);
        doc.setStatus(status);
        doc.setApplicationId(TEST_APPLICATION_ID);
        doc.setUserId(TEST_USER_ID);
        return doc;
    }
}