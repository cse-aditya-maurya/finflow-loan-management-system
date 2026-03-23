package com.finflow.document.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.document.entity.Document;
import com.finflow.document.service.DocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService service;

    @PostMapping("/upload")
    public Document upload(@RequestBody Document doc) {
        return service.upload(doc);
    }

    @GetMapping("/application/{id}")
    public List<Document> byApplication(
            @PathVariable Long id) {
        return service.getByApplication(id);
    }

    @PutMapping("/{id}/verify")
    public Document verify(@PathVariable Long id) {
        return service.verify(id);
    }
}