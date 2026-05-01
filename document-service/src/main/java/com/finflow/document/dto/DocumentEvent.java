package com.finflow.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
//@NoArgsConstructor
@Builder
public class DocumentEvent {
    private String eventType;
    private String userId;
    private String applicationId;
    private String documentType;
    private String status;
    private String remarks;
    private String email; // For notification purposes
    private String name;
    private String message;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public DocumentEvent() {
    }

    public DocumentEvent(String eventType, String userId, String applicationId, String documentType, String status, String remarks, String email, String name, String message) {
        this.eventType = eventType;
        this.userId = userId;
        this.applicationId = applicationId;
        this.documentType = documentType;
        this.status = status;
        this.remarks = remarks;
        this.email = email;
        this.name = name;
        this.message = message;
    }

}
