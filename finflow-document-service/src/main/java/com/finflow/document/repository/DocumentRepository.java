package com.finflow.document.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.finflow.document.entity.Document;

public interface DocumentRepository
extends JpaRepository<Document, Long> {

List<Document> findByApplicationId(Long appId);

List<Document> findByApplicantEmail(String email);
}