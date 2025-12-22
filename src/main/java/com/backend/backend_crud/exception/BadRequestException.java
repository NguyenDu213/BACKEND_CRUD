package com.backend.backend_crud.exception;

/**
 * Exception được throw khi request không hợp lệ
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
