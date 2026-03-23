package com.finflow.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "FINFLOW-APPLICATION-SERVICE")
public interface ApplicationClient {

    @PutMapping("/applications/internal/{id}/status")
    void updateStatus(
            @PathVariable Long id,
            @RequestParam String status
    );
}