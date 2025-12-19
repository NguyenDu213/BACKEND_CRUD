package com.backend.backend_crud.exception;

/**
 * Exception cho các lỗi liên quan đến JWT token
 */
public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}
