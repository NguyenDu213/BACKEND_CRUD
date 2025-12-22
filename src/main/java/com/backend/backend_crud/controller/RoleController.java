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

    /**
     * Lấy danh sách tất cả roles với filter tùy chọn
     *
     * @param typeRole Loại role (PROVIDER hoặc SCHOOL) - optional
     * @param schoolId ID của school - optional
     * @return Danh sách RoleResponse
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles(
            @RequestParam(required = false) RoleType typeRole,
            @RequestParam(required = false) Long schoolId) {
        List<RoleResponse> roles = roleService.getAllRoles(typeRole, schoolId);
        return ResponseEntity.ok(ApiResponse.<List<RoleResponse>>builder()
                .status(true)
                .message("Lấy danh sách roles thành công")
                .data(roles)
                .build());
    }

    /**
     * Lấy role theo ID
     *
     * @param id ID của role
     * @return RoleResponse
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable Long id) {
        RoleResponse role = roleService.getRoleById(id);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .status(true)
                .message("Lấy role thành công")
                .data(role)
                .build());
    }

    /**
     * Tạo role mới
     *
     * @param request RoleRequest chứa thông tin role
     * @return RoleResponse
     */
    @PostMapping
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        // TODO: Lấy createBy từ security context khi có authentication
        // Tạm thời dùng giá trị mặc định hoặc từ request header
        Long createBy = 1L; // Sẽ được thay thế bằng user ID từ JWT token
        
        RoleResponse role = roleService.createRole(request, createBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RoleResponse>builder()
                        .status(true)
                        .message("Tạo role thành công")
                        .data(role)
                        .build());
    }

    /**
     * Cập nhật role
     *
     * @param id      ID của role cần cập nhật
     * @param request UpdateRoleRequest chứa thông tin cập nhật
     * @return RoleResponse
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoleRequest request) {
        // TODO: Lấy updateBy từ security context khi có authentication
        // Tạm thời dùng giá trị mặc định hoặc từ request header
        Long updateBy = 1L;
        
        RoleResponse role = roleService.updateRole(id, request, updateBy);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .status(true)
                .message("Cập nhật role thành công")
                .data(role)
                .build());
    }

    /**
     * Xóa role
     *
     * @param id ID của role cần xóa
     * @return ApiResponse với thông báo thành công
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.builder()
                .status(true)
                .message("Xóa role thành công")
                .data(null)
                .build());
    }
}
