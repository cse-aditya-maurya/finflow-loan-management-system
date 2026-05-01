package com.finflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpEmailEvent {
    private String eventType;   // OTP_CREATED
    private String email;
    private String name;
    private String message;
    private String otp;
}
