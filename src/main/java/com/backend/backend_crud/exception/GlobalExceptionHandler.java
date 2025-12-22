package com.backend.backend_crud.exception;

import com.backend.backend_crud.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
 * Tất cả lỗi được log chi tiết và trả về response rõ ràng
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * Xử lý AuthenticationException
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e) {
                logError("Authentication Error", e, HttpStatus.UNAUTHORIZED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý TokenException
         */
        @ExceptionHandler(TokenException.class)
        public ResponseEntity<ApiResponse<Object>> handleTokenException(TokenException e) {
                logError("Token Error", e, HttpStatus.UNAUTHORIZED);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý ResourceNotFoundException
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException e) {
                logError("Resource Not Found", e, HttpStatus.NOT_FOUND);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý ForbiddenException
         */
        @ExceptionHandler(ForbiddenException.class)
        public ResponseEntity<ApiResponse<Object>> handleForbiddenException(ForbiddenException e) {
                logError("Forbidden", e, HttpStatus.FORBIDDEN);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý BadRequestException
         */
        @ExceptionHandler(BadRequestException.class)
        public ResponseEntity<ApiResponse<Object>> handleBadRequestException(BadRequestException e) {
                logError("Bad Request", e, HttpStatus.BAD_REQUEST);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý ConflictException
         */
        @ExceptionHandler(ConflictException.class)
        public ResponseEntity<ApiResponse<Object>> handleConflictException(ConflictException e) {
                logError("Conflict", e, HttpStatus.CONFLICT);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý validation errors (MethodArgumentNotValidException)
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
                        MethodArgumentNotValidException e) {
                Map<String, String> errors = new HashMap<>();
                e.getBindingResult().getAllErrors().forEach((error) -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });
                logError("Validation Error", e, HttpStatus.BAD_REQUEST);
                log.warn("Validation errors details: {}", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.<Map<String, String>>builder()
                                                .status(false)
                                                .message("Dữ liệu không hợp lệ")
                                                .data(errors)
                                                .build());
        }

        /**
         * Xử lý IllegalArgumentException
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
                logError("Illegal Argument", e, HttpStatus.BAD_REQUEST);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý DataIntegrityViolationException (database constraint violations)
         */
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolationException(
                        DataIntegrityViolationException e) {
                logError("Data Integrity Violation", e, HttpStatus.CONFLICT);
                String message = extractConstraintViolationMessage(e);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message(message)
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý RuntimeException (fallback)
         */
        @ExceptionHandler(RuntimeException.class)
        public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
                logError("Runtime Error", e, HttpStatus.INTERNAL_SERVER_ERROR);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message("Đã xảy ra lỗi: " + e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Xử lý Exception (catch-all)
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
                logError("Unexpected Error", e, HttpStatus.INTERNAL_SERVER_ERROR);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.builder()
                                                .status(false)
                                                .message("Đã xảy ra lỗi không mong đợi: " + e.getMessage())
                                                .data(null)
                                                .build());
        }

        /**
         * Helper method để log lỗi chi tiết
         */
        private void logError(String errorType, Exception e, HttpStatus status) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String stackTrace = sw.toString();

                log.error("==========================================");
                log.error("ERROR TYPE: {}", errorType);
                log.error("HTTP STATUS: {} ({})", status.value(), status.getReasonPhrase());
                log.error("ERROR MESSAGE: {}", e.getMessage());
                log.error("EXCEPTION CLASS: {}", e.getClass().getName());
                log.error("STACK TRACE:\n{}", stackTrace);
                log.error("==========================================");
        }

        /**
         * Helper method để extract message từ DataIntegrityViolationException
         */
        private String extractConstraintViolationMessage(DataIntegrityViolationException e) {
                String message = e.getMessage();
                if (message == null) {
                        return "Vi phạm ràng buộc dữ liệu";
                }

                // Xử lý các trường hợp phổ biến
                if (message.contains("Duplicate entry")) {
                        return "Dữ liệu đã tồn tại trong hệ thống";
                } else if (message.contains("foreign key constraint")) {
                        return "Không thể thực hiện thao tác do ràng buộc khóa ngoại";
                } else if (message.contains("unique constraint")) {
                        return "Dữ liệu đã tồn tại (vi phạm ràng buộc duy nhất)";
                } else if (message.contains("not-null constraint")) {
                        return "Thiếu thông tin bắt buộc";
                }

                // Trả về message gốc nếu không match pattern nào
                return "Vi phạm ràng buộc dữ liệu: " + message;
        }
}
