package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.UserResponse;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.service.JwtService;
import com.backend.backend_crud.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;
    private final HttpServletRequest httpServletRequest;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAll() {
        Long currentUserId = getCurrentUserId();

        return ResponseEntity.ok(userService.getAll(currentUserId));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam(required = false) String keyword) {
        Long currentUserId = getCurrentUserId();

        return ResponseEntity.ok(userService.searchUser(keyword, currentUserId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserRequest request) {
        Long currentUserId = getCurrentUserId();

        return ResponseEntity.ok(userService.createUser(request, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
        Long currentUserId = getCurrentUserId();

        return ResponseEntity.ok(userService.updateUser(request, id, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUser(
            @Valid @PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    /**
     * Lấy danh sách users theo roleId
     * GET /api/users/by-role/{roleId}
     */
    @GetMapping("/by-role/{roleId}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRoleId(@PathVariable Long roleId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(userService.getUsersByRoleId(roleId, currentUserId));
    }

    /**
     * Kiểm tra role có đang được sử dụng không
     * GET /api/users/role-in-use/{roleId}
     */
    @GetMapping("/role-in-use/{roleId}")
    public ResponseEntity<ApiResponse<Boolean>> isRoleInUse(@PathVariable Long roleId) {
        getCurrentUserId();
        return ResponseEntity.ok(userService.isRoleInUse(roleId));
    }

    /**
     * Gán role mới cho tất cả users đang dùng role cũ
     * PUT /api/users/reassign-role?oldRoleId={oldRoleId}&newRoleId={newRoleId}
     */
    @PutMapping("/reassign-role")
    public ResponseEntity<ApiResponse<String>> reassignRole(
            @RequestParam Long oldRoleId,
            @RequestParam Long newRoleId) {
        Long currentUserId = getCurrentUserId();
        return ResponseEntity.ok(userService.reassignRole(oldRoleId, newRoleId, currentUserId));
    }

    private Long getCurrentUserId() {
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException.TokenException("Token không hợp lệ");
        }

        String token = authHeader.substring(7);
        jwtService.validateAccessToken(token);
        return jwtService.getUserIdFromToken(token);
    }
}
