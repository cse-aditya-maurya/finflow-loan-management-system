package com.finflow.document.dto;

import com.finflow.document.model.DocumentType;

import lombok.Data;

@Data
public class UploadDocumentRequest {

    private Long applicationId;
    private DocumentType documentType;
    private String fileUrl;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }


    public UploadDocumentRequest() {
    }

}
