package com.backend.backend_crud.exception;

import com.backend.backend_crud.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler để xử lý tất cả exceptions trong ứng dụng
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        // ========== Custom Exceptions ==========

        @ExceptionHandler({ AppException.AuthenticationException.class, AppException.TokenException.class })
        public ResponseEntity<ApiResponse<Object>> handleUnauthorizedException(Exception e) {
                return buildErrorResponse(e, HttpStatus.UNAUTHORIZED, "Authentication Error");
        }

        @ExceptionHandler(AppException.ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(
                        AppException.ResourceNotFoundException e) {
                return buildErrorResponse(e, HttpStatus.NOT_FOUND, "Resource Not Found");
        }

        @ExceptionHandler({ AppException.ForbiddenException.class, AccessDeniedException.class })
        public ResponseEntity<ApiResponse<Object>> handleForbiddenException(Exception e) {
                String message = e instanceof AccessDeniedException
                                ? "Bạn không có quyền truy cập tài nguyên này"
                                : e.getMessage();
                return buildErrorResponse(e, HttpStatus.FORBIDDEN, "Forbidden", message);
        }

        @ExceptionHandler({ AppException.BadRequestException.class, IllegalArgumentException.class })
        public ResponseEntity<ApiResponse<Object>> handleBadRequestException(Exception e) {
                return buildErrorResponse(e, HttpStatus.BAD_REQUEST, "Bad Request");
        }

        @ExceptionHandler(AppException.ConflictException.class)
        public ResponseEntity<ApiResponse<Object>> handleConflictException(AppException.ConflictException e) {
                return buildErrorResponse(e, HttpStatus.CONFLICT, "Conflict");
        }

        // ========== Validation & Database Exceptions ==========

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
                        MethodArgumentNotValidException e) {
                Map<String, String> errors = new HashMap<>();
                e.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                logError("Validation Error", e, HttpStatus.BAD_REQUEST);
                log.warn("Validation errors: {}", errors);

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Map<String, String>>builder()
                                                .status(false)
                                                .message("Dữ liệu không hợp lệ")
                                                .data(errors)
                                                .build());
        }

        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleCustomValidation(ValidationException ex) {
                logError("Custom Validation Error", ex, HttpStatus.BAD_REQUEST);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                                ApiResponse.<Map<String, String>>builder()
                                                .status(false)
                                                .message(ex.getMessage())
                                                .data(ex.getErrors())
                                                .build());
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
                        DataIntegrityViolationException e) {
                String message = extractConstraintMessage(e);
                return buildErrorResponse(e, HttpStatus.CONFLICT, "Data Integrity Violation", message);
        }

        // ========== Fallback Handlers ==========

        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
                return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, "Runtime Error",
                                "Đã xảy ra lỗi: " + e.getMessage());
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
                return buildErrorResponse(e, HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected Error",
                                "Đã xảy ra lỗi không mong đợi: " + e.getMessage());
        }

        // ========== Helper Methods ==========

        /**
         * Build error response với message từ exception
         */
        private ResponseEntity<ApiResponse<Object>> buildErrorResponse(
                        Exception e, HttpStatus status, String errorType) {
                return buildErrorResponse(e, status, errorType, e.getMessage());
        }

        /**
         * Build error response với custom message
         */
        private ResponseEntity<ApiResponse<Object>> buildErrorResponse(
                        Exception e, HttpStatus status, String errorType, String message) {
                logError(errorType, e, status);
                return ResponseEntity.status(status)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(message)
                                                .data(null)
                                                .build());
        }

        /**
         * Log lỗi chi tiết
         */
        private void logError(String errorType, Exception e, HttpStatus status) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));

                log.error("==========================================");
                log.error("ERROR TYPE: {}", errorType);
                log.error("HTTP STATUS: {} ({})", status.value(), status.getReasonPhrase());
                log.error("ERROR MESSAGE: {}", e.getMessage());
                log.error("EXCEPTION CLASS: {}", e.getClass().getName());
                log.error("STACK TRACE:\n{}", sw);
                log.error("==========================================");
        }

        /**
         * Extract message từ DataIntegrityViolationException
         */
        private String extractConstraintMessage(DataIntegrityViolationException e) {
                String message = e.getMessage();
                if (message == null) {
                        return "Vi phạm ràng buộc dữ liệu";
                }

                if (message.contains("Duplicate entry")) {
                        return "Dữ liệu đã tồn tại trong hệ thống";
                } else if (message.contains("foreign key constraint")) {
                        return "Không thể thực hiện thao tác do ràng buộc khóa ngoại";
                } else if (message.contains("unique constraint")) {
                        return "Dữ liệu đã tồn tại (vi phạm ràng buộc duy nhất)";
                } else if (message.contains("not-null constraint")) {
                        return "Thiếu thông tin bắt buộc";
                }

                return "Vi phạm ràng buộc dữ liệu: " + message;
        }
}
