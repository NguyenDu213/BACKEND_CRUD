package com.backend.backend_crud.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSchoolRequest {

    @Size(min = 2, max = 200, message = "Tên trường phải từ 2 đến 200 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s0-9().,-]+$", message = "Tên trường chứa ký tự không hợp lệ")
    private String name;

    @Size(min = 2, max = 50, message = "Mã trường phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Mã trường chỉ được chứa chữ hoa, số và dấu gạch dưới")
    private String code;

    @Email(message = "Email không hợp lệ", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    @Size(max = 100, message = "Email không được quá 100 ký tự")
    private String email;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Hotline không hợp lệ. Định dạng: 0xxxxxxxxx hoặc +84xxxxxxxxx")
    private String hotline;

    @Size(min = 5, max = 255, message = "Địa chỉ phải từ 5 đến 255 ký tự")
    private String address;

    @Size(min = 2, max = 100, message = "Tên hiệu trưởng phải từ 2 đến 100 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Tên hiệu trưởng chỉ được chứa chữ cái và khoảng trắng")
    private String principalName;
}

