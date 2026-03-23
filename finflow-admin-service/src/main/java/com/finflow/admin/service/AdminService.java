package com.finflow.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.finflow.admin.client.ApplicationClient;
import com.finflow.admin.entity.Decision;
import com.finflow.admin.repository.DecisionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final DecisionRepository repo;
    private final ApplicationClient client;

    public Decision decide(Decision d) {

        Decision saved = repo.save(d);

        if(d.getDecision().equals("APPROVED")) {

            client.updateStatus(
                    d.getApplicationId(),
                    "APPROVED");

        } else {

            client.updateStatus(
                    d.getApplicationId(),
                    "REJECTED");
        }

        return saved;
    }
    public List<Decision> byApplication(Long id) {
        return repo.findByApplicationId(id);
    }
}