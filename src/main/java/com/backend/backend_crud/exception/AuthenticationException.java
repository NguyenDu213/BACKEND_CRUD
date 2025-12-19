package com.backend.backend_crud.exception;

/**
 * Exception cho các lỗi xác thực (authentication)
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
