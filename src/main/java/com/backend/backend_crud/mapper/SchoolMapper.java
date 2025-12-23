package com.backend.backend_crud.mapper;

import com.backend.backend_crud.dto.request.SchoolRequest;
import com.backend.backend_crud.dto.request.UpdateSchoolRequest;
import com.backend.backend_crud.dto.response.SchoolResponse;
import com.backend.backend_crud.entity.School;
import org.springframework.stereotype.Component;

@Component
public class SchoolMapper {

    public School mapToEntity(SchoolRequest request) {
        return School.builder()
                .email(request.getEmail())
                .hotline(request.getHotline())
                .address(request.getAddress())
                .principalName(request.getPrincipalName())
                .name(request.getName())
                .code(request.getCode())
                .build();
    }

    public SchoolResponse mapToResponse(School school) {
        return SchoolResponse.builder()
                .id(school.getId())
                .email(school.getEmail())
                .hotline(school.getHotline())
                .address(school.getAddress())
                .principalName(school.getPrincipalName())
                .name(school.getName())
                .code(school.getCode())
                .createdAt(school.getCreatedAt())
                .updatedAt(school.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(School school, UpdateSchoolRequest request) {
        if (request.getCode() != null) school.setCode(request.getCode());
        if (request.getName() != null) school.setName(request.getName());
        if (request.getEmail() != null) school.setEmail(request.getEmail());
        if (request.getHotline() != null) school.setHotline(request.getHotline());
        if (request.getAddress() != null) school.setAddress(request.getAddress());
        if (request.getPrincipalName() != null) school.setPrincipalName(request.getPrincipalName());
    }
}