package com.backend.backend_crud.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchoolRequest {

    @NotBlank(message = "Tên trường không được để trống")
    @Size(min = 2, max = 200, message = "Tên trường phải từ 2 đến 200 ký tự")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Tên trường chỉ được chứa chữ cái (chữ hoa và chữ thường) và khoảng trắng. Không được chứa số, ký tự đặc biệt hoặc dấu tiếng Việt (ví dụ: \"Trường THPT ABC\" - hợp lệ, \"Trường THPT 123\" - không hợp lệ)")
    private String name;

    @NotBlank(message = "Mã trường không được để trống")
    @Size(min = 2, max = 50, message = "Mã trường phải từ 2 đến 50 ký tự")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "Mã trường chỉ được chứa chữ in hoa và số (không có dấu gạch dưới, ví dụ: TH001, ABC123)")
    private String code;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ", regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    @Size(max = 100, message = "Email không được quá 100 ký tự")
    private String email;

    @NotBlank(message = "Hotline không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Hotline không hợp lệ. Chỉ được phép chứa số, bắt đầu bằng 0 và có 10 chữ số (ví dụ: 0123456789)")
    private String hotline;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(min = 5, max = 255, message = "Địa chỉ phải từ 5 đến 255 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9\\s/,.-]+$", message = "Địa chỉ chỉ được chứa chữ cái, số, khoảng trắng và các ký tự: / , . -")
    private String address;

    @NotBlank(message = "Tên hiệu trưởng không được để trống")
    @Size(min = 2, max = 100, message = "Tên hiệu trưởng phải từ 2 đến 100 ký tự")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Tên hiệu trưởng chỉ được chứa chữ cái (chữ hoa và chữ thường) và khoảng trắng. Không được chứa số, ký tự đặc biệt hoặc dấu tiếng Việt (ví dụ: \"Nguyen Van A\" - hợp lệ, \"Nguyễn Văn A\" - không hợp lệ)")
    private String principalName;
}