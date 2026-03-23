//package com.finflow.application.controller;
//
//import java.util.List;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.finflow.application.entity.LoanApplication;
//import com.finflow.application.service.LoanApplicationService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/applications")
//@RequiredArgsConstructor
//public class LoanApplicationController {
//
//    private final LoanApplicationService service;
//
//    @PostMapping
//    public LoanApplication create(@RequestBody LoanApplication app) {
//        return service.create(app);
//    }
//
//    @PostMapping("/{id}/submit")
//    public LoanApplication submit(@PathVariable Long id) {
//        return service.submit(id);
//    }
//
//    @GetMapping("/my")
//    public List<LoanApplication> myApps(
//            @RequestParam String email) {
//        return service.myApps(email);
//    }
//    
//    @PutMapping("/internal/applications/{id}/status")
//    public void updateStatus(
//            @PathVariable Long id,
//            @RequestParam String status) {
//
//        LoanApplication app = repo.findById(id).orElseThrow();
//        app.setStatus(status);
//        repo.save(app);
//    }
//}






package com.finflow.application.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.finflow.application.entity.LoanApplication;
import com.finflow.application.service.LoanApplicationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService service;

    // ✅ Create Application
    @PostMapping
    public LoanApplication create(@RequestBody LoanApplication app) {
        return service.create(app);
    }

    // ✅ Submit Application
    @PostMapping("/{id}/submit")
    public LoanApplication submit(@PathVariable Long id) {
        return service.submit(id);
    }

    // ✅ Get Applications of Applicant
    @GetMapping("/my")
    public List<LoanApplication> myApps(
            @RequestParam String email) {
        return service.myApps(email);
    }

    // ✅ INTERNAL API (called by Admin Service via Feign)
    @PutMapping("/internal/{id}/status")
    public void updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        service.updateStatus(id, status);
    }
    
    @GetMapping("/internal/all")
    public List<LoanApplication> all() {
        return service.getAll();
    }
    
   
}




























