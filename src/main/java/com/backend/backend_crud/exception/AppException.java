package com.backend.backend_crud.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppException extends RuntimeException {
    private final HttpStatus status;
    private final int code;

    public AppException(int code, String message) {
        super(message);
        this.code = code;
        this.status = HttpStatus.valueOf(code);
    }
}