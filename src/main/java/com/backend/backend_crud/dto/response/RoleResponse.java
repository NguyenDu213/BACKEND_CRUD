package com.backend.backend_crud.dto.response;

import com.backend.backend_crud.entity.RoleType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponse {
    private Long id;
    private String roleName;
    private RoleType typeRole;
    private String description;
    private Long schoolId;
    private String schoolName;
    private Long userCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}