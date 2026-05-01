package com.finflow.auth.exception;

import com.finflow.auth.dto.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleAuthException_ReturnsUnauthorized() {
        AuthException exception = new AuthException("Invalid credentials");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleAuthException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid credentials", response.getBody().getMessage());
    }

    @Test
    void handleRuntimeException_ReturnsBadRequest() {
        RuntimeException exception = new RuntimeException("Something went wrong");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleRuntimeException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Something went wrong", response.getBody().getMessage());
    }
}
