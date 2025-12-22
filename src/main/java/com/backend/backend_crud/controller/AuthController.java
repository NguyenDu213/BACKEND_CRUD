package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.LoginRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.JwtResponse;
import com.backend.backend_crud.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý authentication
 * Tất cả exceptions được xử lý bởi GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Đăng nhập và nhận JWT token
     *
     * @param loginRequest Thông tin đăng nhập (email, password)
     * @return JwtResponse chứa access token
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        JwtResponse jwtResponse = authService.login(loginRequest.getEmail(), loginRequest.getPassword());
        return ResponseEntity.ok(ApiResponse.<JwtResponse>builder()
                .status(true)
                .message("Đăng nhập thành công")
                .data(jwtResponse)
                .build());
    }


}
