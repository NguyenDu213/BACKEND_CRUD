package com.backend.backend_crud.dto.request;

import com.backend.backend_crud.entity.Gender;
import com.backend.backend_crud.entity.UserScope;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    private String fullName;

    @NotNull(message = "Giới tính không được để trống")
    private Gender gender;

    @NotNull(message = "Năm sinh không được để trống")
    @Past(message = "Năm sinh phải là ngày trong quá khứ")
    private LocalDateTime birthYear;

    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6 đến 100 ký tự")
    private String password;

    @NotNull(message = "Phạm vi người dùng không được để trống")
    private UserScope scope;

    private Long schoolId;

    @NotNull(message = "Role không được để trống")
    private Long roleId;
}