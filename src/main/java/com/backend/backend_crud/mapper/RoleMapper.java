package com.backend.backend_crud.mapper;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.School;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role mapToEntity(RoleRequest request, School school) {
        return Role.builder()
                .roleName(request.getRoleName())
                .typeRole(request.getTypeRole())
                .description(request.getDescription())
                .school(school)
                .build();
    }

    public RoleResponse mapToResponse(Role role, Long userCount) {
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .typeRole(role.getTypeRole())
                .description(role.getDescription())
                .schoolId(role.getSchool() != null ? role.getSchool().getId() : null)
                .schoolName(role.getSchool() != null ? role.getSchool().getName() : null)
                .userCount(userCount)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }
}