package com.finflow.document.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.finflow.document.entity.Document;
import com.finflow.document.repository.DocumentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository repo;

    public Document upload(Document doc) {
        doc.setStatus("PENDING");
        return repo.save(doc);
    }

    public List<Document> getByApplication(Long id) {
        return repo.findByApplicationId(id);
    }

    public Document verify(Long id) {
        Document d = repo.findById(id).orElseThrow();
        d.setStatus("VERIFIED");
        return repo.save(d);
    }
}