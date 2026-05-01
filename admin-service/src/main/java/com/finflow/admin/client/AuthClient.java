package com.finflow.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "AUTH-SERVICE", path = "/auth")
public interface AuthClient {

    @GetMapping("/user/email/{email}")
    Long getUserIdByEmail(
            @RequestHeader("Authorization") String token,
            @PathVariable("email") String email);
    
    @GetMapping("/admin/all-users")
    Object getAllUsers(@RequestHeader("Authorization") String token);
    
    @GetMapping("/admin/user/{userId}")
    Object getUserById(
            @RequestHeader("Authorization") String token,
            @PathVariable("userId") Long userId);
}
