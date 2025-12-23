package com.backend.backend_crud.service;

import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.RoleType;

import java.util.List;

public interface RoleService {
    ApiResponse<List<RoleResponse>> getAllRoles(RoleType typeRole, Long schoolId);

    ApiResponse<RoleResponse> getRoleById(Long id);

    ApiResponse<RoleResponse> getRoleByName(String roleName, RoleType typeRole, Long schoolId);

    ApiResponse<RoleResponse> createRole(RoleResponse request, Long createBy);

    ApiResponse<RoleResponse> updateRole(Long id, RoleResponse request, Long updateBy);

    ApiResponse<String> deleteRole(Long id);
}
