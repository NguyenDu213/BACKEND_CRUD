package com.backend.backend_crud.dto.response;

import com.backend.backend_crud.entity.Gender;
import com.backend.backend_crud.entity.UserScope;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private Gender gender;
    private LocalDateTime birthYear;
    private String address;
    private String phoneNumber;
    private String email;
    private Boolean isActive;
    private UserScope scope;
    private Long schoolId;
    private String schoolName;
    private Long roleId;
    private String roleName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}