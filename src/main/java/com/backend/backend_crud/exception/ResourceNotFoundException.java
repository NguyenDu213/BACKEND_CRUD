package com.backend.backend_crud.exception;

/**
 * Exception được throw khi không tìm thấy resource
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
