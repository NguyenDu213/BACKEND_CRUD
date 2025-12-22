package com.backend.backend_crud.exception;

/**
 * Exception được throw khi user không có quyền truy cập
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
