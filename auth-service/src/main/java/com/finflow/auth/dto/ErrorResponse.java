package com.finflow.auth.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private boolean success;
    private String message;

    public static ErrorResponse of(String message) {
        return new ErrorResponse(false, message);
    }
}



