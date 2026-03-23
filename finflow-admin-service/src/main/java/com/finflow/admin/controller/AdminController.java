package com.finflow.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.admin.entity.Decision;
import com.finflow.admin.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService service;

    @PostMapping("/applications/{id}/decision")
    public Decision decide(	
            @PathVariable Long id,
            @RequestBody Decision d) {

        d.setApplicationId(id);
        return service.decide(d);
    }

    @GetMapping("/applications/{id}")
    public List<Decision> decisions(
            @PathVariable Long id) {
        return service.byApplication(id);
    }
}