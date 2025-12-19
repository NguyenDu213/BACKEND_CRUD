package com.backend.backend_crud.dto.request;

import com.backend.backend_crud.entity.RoleType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoleRequest {

    @Size(min = 2, max = 50, message = "Tên role phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Tên role chỉ được chứa chữ hoa, số và dấu gạch dưới (ví dụ: ADMIN_USER, TEACHER_ROLE)")
    private String roleName;

    private RoleType typeRole;

    @Size(min = 5, max = 500, message = "Mô tả phải từ 5 đến 500 ký tự")
    private String description;

    private Long schoolId;

    @AssertTrue(message = "schoolId là bắt buộc khi typeRole là SCHOOL")
    public boolean isValidSchoolId() {
        if (typeRole != null && typeRole == RoleType.SCHOOL) {
            return schoolId != null && schoolId > 0;
        }
        return true;
    }
}

