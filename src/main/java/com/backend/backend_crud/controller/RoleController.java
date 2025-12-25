package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.request.UpdateRoleRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
@RequiredArgsConstructor
public class RoleController {

        private final RoleService roleService;

        @GetMapping
        public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles(
                        @RequestParam(required = false) RoleType typeRole,
                        @RequestParam(required = false) Long schoolId) {
                return ResponseEntity.ok(roleService.getAllRoles(typeRole, schoolId));
        }

        @GetMapping("/{id}")
        public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
                return ResponseEntity.ok(roleService.getRoleById(id));
        }

        @GetMapping("/name/{roleName}")
        public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(
                        @PathVariable String roleName,
                        @RequestParam(required = false) RoleType typeRole,
                        @RequestParam(required = false) Long schoolId) {
                return ResponseEntity.ok(roleService.getRoleByName(roleName, typeRole, schoolId));
        }

        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<RoleResponse>>> searchRoles(
                        @RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Long schoolId,
                        @RequestParam(required = false) RoleType typeRole) {
                return ResponseEntity.ok(roleService.searchRoles(keyword, schoolId, typeRole));
        }

        /**
         * Tạo role mới
         * Service yêu cầu tham số createBy, ta lấy từ SecurityContext
         */
        @PostMapping
        public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
                ApiResponse<RoleResponse> response = roleService.createRole(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Cập nhật role
         */
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
                @PathVariable Long id,
                @Valid @RequestBody UpdateRoleRequest request) { // Nhận đúng DTO Update
                return ResponseEntity.ok(roleService.updateRole(id, request));
        }

        /**
         * Xóa role
         * Service mới đã tự gọi getCurrentUser() bên trong để check quyền xóa,
         * nên Controller chỉ cần truyền ID role là đủ.
         */
        @DeleteMapping("/{id}")
        // @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
        public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
                return ResponseEntity.ok(roleService.deleteRole(id));
        }

        /**
         * Gán role mới cho users và xóa role cũ
         * POST /api/roles/{oldRoleId}/reassign-and-delete?newRoleId={newRoleId}
         */
        @PostMapping("/{oldRoleId}/reassign-and-delete")
        public ResponseEntity<ApiResponse<String>> reassignRoleAndDelete(
                        @PathVariable Long oldRoleId,
                        @RequestParam Long newRoleId) {
                return ResponseEntity.ok(roleService.reassignRoleAndDelete(oldRoleId, newRoleId));
        }


}
