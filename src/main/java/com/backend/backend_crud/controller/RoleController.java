package com.backend.backend_crud.controller;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.request.UpdateRoleRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.mapper.RoleMapper;
import com.backend.backend_crud.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
@RequiredArgsConstructor
public class RoleController {

        private final RoleService roleService;
        private final RoleMapper roleMapper;


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

        /**
         * Tạo role mới
         * Service yêu cầu tham số createBy, ta lấy từ SecurityContext
         */
        @PostMapping
        public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
                // Lấy ID user hiện tại từ Security Context (nhanh, gọn, chuẩn)
                // Long currentUserId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
                Long currentUserId = getCurrentUserId();

                RoleResponse roleResponse = roleMapper.mapToResponse(request);
                ApiResponse<RoleResponse> response = roleService.createRole(roleResponse, currentUserId);

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        /**
         * Cập nhật role
         */
        @PutMapping("/{id}")
        public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
                @PathVariable Long id,
                @Valid @RequestBody UpdateRoleRequest request) {
                // Lấy ID user hiện tại
                // Long currentUserId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
                Long currentUserId = getCurrentUserId();

                RoleResponse roleResponse = roleMapper.mapToResponse(request);
                return ResponseEntity.ok(roleService.updateRole(id, roleResponse, currentUserId));
        }

        /**
         * Xóa role
         * Service mới đã tự gọi getCurrentUser() bên trong để check quyền xóa,
         * nên Controller chỉ cần truyền ID role là đủ.
         */
        @DeleteMapping("/{id}")
        // Nếu muốn chặn School Admin xóa role ngay tại Controller thì bỏ comment dòng dưới:
        // @PreAuthorize("hasAuthority('SYSTEM_ADMIN')")
        public ResponseEntity<ApiResponse<String>> deleteRole(@PathVariable Long id) {
                return ResponseEntity.ok(roleService.deleteRole(id));
        }

        private Long getCurrentUserId() {
                var authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new AppException.TokenException("Không tìm thấy thông tin xác thực");
                }
                try {
                        return Long.valueOf(authentication.getName());
                } catch (NumberFormatException e) {
                        throw new AppException.TokenException("User ID trong token không hợp lệ");
                }
        }

}
