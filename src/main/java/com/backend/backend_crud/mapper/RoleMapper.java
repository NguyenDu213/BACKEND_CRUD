package com.backend.backend_crud.mapper;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.request.UpdateRoleRequest;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.School;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    /**
     * Map từ RoleRequest -> Entity (dùng cho create)
     */
    public Role mapToEntity(RoleRequest request, School school) {
        if (request == null) {
            return null;
        }
        return Role.builder()
                .roleName(request.getRoleName())
                .typeRole(request.getTypeRole())
                .description(request.getDescription())
                .school(school)
                .build();
    }


    /**
     * Cập nhật Entity từ RoleResponse (dùng cho update)
     */
    public void updateEntityFromRequest(Role role, UpdateRoleRequest request) {
        if (request.getRoleName() != null) role.setRoleName(request.getRoleName());
        if (request.getDescription() != null) role.setDescription(request.getDescription());
    }

    /**
     * Map từ Entity -> RoleResponse
     */
    public RoleResponse mapToResponse(Role role, Long userCount) {
        if (role == null) {
            return null;
        }
        return RoleResponse.builder()
                .id(role.getId())
                .roleName(role.getRoleName())
                .typeRole(role.getTypeRole())
                .description(role.getDescription())
                .schoolId(role.getSchool() != null ? role.getSchool().getId() : null)
                .schoolName(role.getSchool() != null ? role.getSchool().getName() : null)
                .userCount(userCount != null ? userCount : 0L)
                .createdAt(role.getCreatedAt())
                .updatedAt(role.getUpdatedAt())
                .build();
    }


}