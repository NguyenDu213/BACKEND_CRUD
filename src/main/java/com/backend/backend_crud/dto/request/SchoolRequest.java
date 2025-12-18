package com.backend.backend_crud.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolRequest {

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Hotline không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Hotline không hợp lệ")
    private String hotline;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    @NotBlank(message = "Tên hiệu trưởng không được để trống")
    @Size(min = 2, max = 100, message = "Tên hiệu trưởng phải từ 2 đến 100 ký tự")
    private String principalName;

    @NotBlank(message = "Tên trường không được để trống")
    @Size(min = 2, max = 200, message = "Tên trường phải từ 2 đến 200 ký tự")
    private String name;

    @NotBlank(message = "Mã trường không được để trống")
    @Size(min = 2, max = 50, message = "Mã trường phải từ 2 đến 50 ký tự")
    private String code;
}