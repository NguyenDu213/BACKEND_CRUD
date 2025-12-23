package com.backend.backend_crud.dto.request;

import com.backend.backend_crud.config.LocalDateTimeDeserializer;
import com.backend.backend_crud.entity.Gender;
import com.backend.backend_crud.entity.UserScope;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequest {

    @Size(min = 2, max = 100, message = "Họ tên phải từ 2 đến 100 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;

    private Gender gender;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime birthYear;

    @Size(min = 5, max = 255, message = "Địa chỉ phải từ 5 đến 255 ký tự")
    private String address;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không hợp lệ. Định dạng: 0xxxxxxxxx hoặc +84xxxxxxxxx")
    private String phoneNumber;


    @Size(max = 100, message = "Email không được quá 100 ký tự")
    private String email;

    private String password;

    private UserScope scope;

    private Long schoolId;

    @Min(value = 1, message = "Role ID phải lớn hơn 0")
    private Long roleId;

    private Boolean isActive;
}

