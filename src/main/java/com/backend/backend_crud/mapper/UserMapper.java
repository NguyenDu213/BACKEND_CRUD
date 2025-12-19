package com.backend.backend_crud.mapper;

import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.UserResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static User mapToEntity(UserRequest request, School school, Role role) {
        return User.builder()
                .fullName(request.getFullName())
                .gender(request.getGender())
                .birthYear(request.getBirthYear())
                .address(request.getAddress())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .password(request.getPassword())
                .isActive(request.getIsActive())
                .scope(request.getScope())
                .school(school)
                .role(role)
                .build();
    }

    public static UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .birthYear(user.getBirthYear())
                .address(user.getAddress())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .scope(user.getScope())
                .schoolId(user.getSchool() != null ? user.getSchool().getId() : null)
                .schoolName(user.getSchool() != null ? user.getSchool().getName() : null)
                .roleId(user.getRole().getId())
                .roleName(user.getRole().getRoleName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}