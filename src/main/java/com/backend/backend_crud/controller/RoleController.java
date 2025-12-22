package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.request.UpdateRoleRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.service.JwtService;
import com.backend.backend_crud.service.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý CRUD operations cho Role
 * Tất cả exceptions được xử lý bởi GlobalExceptionHandler
 */
@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

        private final RoleService roleService;
        private final JwtService jwtService;
        private final HttpServletRequest httpServletRequest;

        /**
         * Lấy danh sách tất cả roles
         * GET /api/roles?typeRole=PROVIDER hoặc /api/roles?typeRole=SCHOOL&schoolId=1
         */
        @GetMapping
        public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles(
                        @RequestParam(required = false) RoleType typeRole,
                        @RequestParam(required = false) Long schoolId) {
                return ResponseEntity.ok(roleService.getAllRoles(typeRole, schoolId));
        }

        /**
         * Lấy role theo ID
         * GET /api/roles/{id}
         */
        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
                return ResponseEntity.ok(roleService.getRoleById(id));
        }

        /**
         * Lấy role theo tên
         * GET /api/roles/name/{roleName}?schoolId=1
         */
        @GetMapping("/name/{roleName}")
        public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
                        @PathVariable String roleName,
                        @RequestParam(required = false) Long schoolId) {
                return ResponseEntity.ok(roleService.getRoleByName(roleName, schoolId));
        }

        /**
         * Tạo role mới
         * POST /api/roles
         */
        @PostMapping
        public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
                Long currentUserId = getCurrentUserId();
                RoleResponse roleResponse = convertToRoleResponse(request);
                ApiResponse<RoleResponse> response = roleService.createRole(roleResponse, currentUserId);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Cập nhật role
         * PUT /api/roles/{id}
         */
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateRoleRequest request) {
                Long currentUserId = getCurrentUserId();
                RoleResponse roleResponse = convertToRoleResponse(request);
                return ResponseEntity.ok(roleService.updateRole(id, roleResponse, currentUserId));
        }

        /**
         * Xóa role
         * DELETE /api/roles/{id}
         */
        @DeleteMapping("/{id}")
        public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
                return ResponseEntity.ok(roleService.deleteRole(id));
        }

        /**
         * Lấy current user ID từ JWT token trong request header
         */
        private Long getCurrentUserId() {
                String authHeader = httpServletRequest.getHeader("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                        throw new AppException.TokenException("Token không hợp lệ");
                }

                String token = authHeader.substring(7);
                jwtService.validateAccessToken(token);
                return jwtService.getUserIdFromToken(token);
        }

        /**
         * Chuyển đổi RoleRequest thành RoleResponse
         */
        private RoleResponse convertToRoleResponse(RoleRequest request) {
                return RoleResponse.builder()
                                .roleName(request.getRoleName())
                                .typeRole(request.getTypeRole())
                                .description(request.getDescription())
                                .schoolId(request.getSchoolId())
                                .build();
        }

        /**
         * Chuyển đổi UpdateRoleRequest thành RoleResponse
         */
        private RoleResponse convertToRoleResponse(UpdateRoleRequest request) {
                return RoleResponse.builder()
                                .roleName(request.getRoleName())
                                .typeRole(request.getTypeRole())
                                .description(request.getDescription())
                                .schoolId(request.getSchoolId())
                                .build();
        }
}
