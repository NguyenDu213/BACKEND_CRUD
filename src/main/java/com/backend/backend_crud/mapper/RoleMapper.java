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
     * Map từ RoleResponse -> Entity (dùng cho create từ Response)
     */
    public Role mapToEntityFromResponse(RoleResponse response, School school) {
        if (response == null) {
            return null;
        }
        return Role.builder()
                .roleName(response.getRoleName())
                .typeRole(response.getTypeRole())
                .description(response.getDescription())
                .school(school)
                .build();
    }

    /**
     * Cập nhật Entity từ RoleResponse (dùng cho update)
     */
    public void updateEntityFromResponse(Role role, RoleResponse response, School school) {
        if (response == null || role == null) {
            return;
        }
        if (response.getRoleName() != null) {
            role.setRoleName(response.getRoleName());
        }
        if (response.getTypeRole() != null) {
            role.setTypeRole(response.getTypeRole());
        }
        if (response.getDescription() != null) {
            role.setDescription(response.getDescription());
        }
        role.setSchool(school);
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

    /**
     * Map từ RoleRequest -> RoleResponse (dùng trong Controller)
     */
    public RoleResponse mapToResponse(RoleRequest request) {
        if (request == null) {
            return null;
        }
        return RoleResponse.builder()
                .roleName(request.getRoleName())
                .typeRole(request.getTypeRole())
                .description(request.getDescription())
                .schoolId(request.getSchoolId())
                .build();
    }

    /**
     * Map từ UpdateRoleRequest -> RoleResponse (dùng trong Controller)
     */
    public RoleResponse mapToResponse(UpdateRoleRequest request) {
        if (request == null) {
            return null;
        }
        return RoleResponse.builder()
                .roleName(request.getRoleName())
                .typeRole(request.getTypeRole())
                .description(request.getDescription())
                .schoolId(request.getSchoolId())
                .build();
    }
}