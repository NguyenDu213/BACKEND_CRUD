package com.backend.backend_crud.exception;

/**
 * Exception được throw khi có conflict (ví dụ: tên đã tồn tại)
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
