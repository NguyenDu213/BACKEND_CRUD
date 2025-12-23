package com.backend.backend_crud.exception;

import lombok.Getter;
import java.util.Map;

@Getter
public class ValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Lỗi validation dữ liệu");
        this.errors = errors;
    }
}