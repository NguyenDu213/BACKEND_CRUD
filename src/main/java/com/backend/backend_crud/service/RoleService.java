package com.backend.backend_crud.service;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.request.UpdateRoleRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.PageResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.RoleType;

import java.util.List;

public interface RoleService {
    ApiResponse<PageResponse<RoleResponse>> getAllRoles(RoleType typeRole, int page, int size);

    ApiResponse<PageResponse<RoleResponse>> searchRoles(String keyword, Long schoolId, RoleType typeRole, int page, int size);

    ApiResponse<RoleResponse> getRoleById(Long id);

    ApiResponse<RoleResponse> getRoleByName(String roleName, RoleType typeRole, Long schoolId);

    ApiResponse<RoleResponse> createRole(RoleRequest request);

    ApiResponse<RoleResponse> updateRole(Long id, UpdateRoleRequest request);

    ApiResponse<String> deleteRole(Long id);

    ApiResponse<String> reassignRoleAndDelete(Long oldRoleId, Long newRoleId);
}
