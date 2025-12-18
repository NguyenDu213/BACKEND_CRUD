package com.backend.backend_crud.dto.request;

import com.backend.backend_crud.entity.RoleType;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {

    @NotBlank(message = "Tên role không được để trống")
    @Size(min = 2, max = 50, message = "Tên role phải từ 2 đến 50 ký tự")
    private String roleName;

    @NotNull(message = "Loại role không được để trống")
    private RoleType typeRole;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    private Long schoolId;
}