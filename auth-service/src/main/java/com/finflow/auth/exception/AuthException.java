package com.finflow.auth.exception;

/**
 * Thrown when authentication fails (wrong email or password).
 * Maps to HTTP 401 Unauthorized.
 */
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }
}
