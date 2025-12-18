# Tóm tắt DTO Validation và Sample Data

## Đã hoàn thành

### 1. Cải thiện Request DTOs với validation đầy đủ

#### UserRequest
- ✅ `fullName`: Validation pattern cho chữ cái và khoảng trắng
- ✅ `birthYear`: Validation ngày trong quá khứ
- ✅ `address`: Tăng min length từ 0 lên 5 ký tự
- ✅ `phoneNumber`: Pattern validation với message rõ ràng
- ✅ `email`: Email validation với regex và max length
- ✅ `password`: Pattern validation yêu cầu chữ hoa, chữ thường và số
- ✅ `schoolId`: Custom validation với `@AssertTrue` - bắt buộc khi scope là SCHOOL
- ✅ `roleId`: Thêm `@Min` validation

#### SchoolRequest
- ✅ `name`: Thêm pattern validation
- ✅ `code`: Pattern validation chỉ cho phép chữ hoa, số và dấu gạch dưới
- ✅ `email`: Email validation với regex và max length
- ✅ `hotline`: Pattern validation với message rõ ràng
- ✅ `address`: Tăng min length từ 0 lên 5 ký tự
- ✅ `principalName`: Pattern validation cho chữ cái và khoảng trắng
- ✅ Sắp xếp lại thứ tự fields theo logic (name, code trước)

#### RoleRequest
- ✅ `roleName`: Pattern validation chỉ cho phép chữ hoa, số và dấu gạch dưới
- ✅ `description`: Tăng min length từ 0 lên 5 ký tự
- ✅ `schoolId`: Custom validation với `@AssertTrue` - bắt buộc khi typeRole là SCHOOL

### 2. Tạo Update DTOs

#### UpdateUserRequest
- ✅ Tất cả fields đều optional (không có `@NotBlank` hoặc `@NotNull`)
- ✅ Giữ nguyên validation rules khi field được cung cấp
- ✅ Thêm field `isActive` để update trạng thái
- ✅ Custom validation cho `schoolId` khi `scope` được cung cấp

#### UpdateSchoolRequest
- ✅ Tất cả fields đều optional
- ✅ Giữ nguyên validation rules khi field được cung cấp

#### UpdateRoleRequest
- ✅ Tất cả fields đều optional
- ✅ Giữ nguyên validation rules khi field được cung cấp
- ✅ Custom validation cho `schoolId` khi `typeRole` được cung cấp

### 3. Tạo Sample Data

#### Files đã tạo:
- ✅ `sample-data/user-requests.json`: Dữ liệu mẫu cho User (create và update)
- ✅ `sample-data/school-requests.json`: Dữ liệu mẫu cho School (create và update)
- ✅ `sample-data/role-requests.json`: Dữ liệu mẫu cho Role (create và update)
- ✅ `sample-data/README.md`: Hướng dẫn sử dụng sample data

## Validation Rules Chi Tiết

### UserRequest/UpdateUserRequest

| Field | Validation Rules |
|-------|----------------|
| `fullName` | Required (create), 2-100 chars, chỉ chữ cái và khoảng trắng |
| `gender` | Required (create), enum: MALE, FEMALE, OTHER |
| `birthYear` | Required (create), LocalDateTime, phải là ngày trong quá khứ |
| `address` | Required (create), 5-255 chars |
| `phoneNumber` | Required (create), pattern: `^(0\|\\+84)[0-9]{9,10}$` |
| `email` | Required (create), email format, max 100 chars |
| `password` | Required (create), 6-100 chars, phải có chữ hoa, chữ thường và số |
| `scope` | Required (create), enum: SCHOOL, PROVIDER |
| `schoolId` | Optional, nhưng bắt buộc nếu scope là SCHOOL |
| `roleId` | Required (create), phải > 0 |

### SchoolRequest/UpdateSchoolRequest

| Field | Validation Rules |
|-------|----------------|
| `name` | Required (create), 2-200 chars, pattern cho ký tự hợp lệ |
| `code` | Required (create), 2-50 chars, chỉ chữ hoa, số và dấu gạch dưới |
| `email` | Required (create), email format, max 100 chars |
| `hotline` | Required (create), pattern: `^(0\|\\+84)[0-9]{9,10}$` |
| `address` | Required (create), 5-255 chars |
| `principalName` | Required (create), 2-100 chars, chỉ chữ cái và khoảng trắng |

### RoleRequest/UpdateRoleRequest

| Field | Validation Rules |
|-------|----------------|
| `roleName` | Required (create), 2-50 chars, chỉ chữ hoa, số và dấu gạch dưới |
| `typeRole` | Required (create), enum: PROVIDER, SCHOOL |
| `description` | Required (create), 5-500 chars |
| `schoolId` | Optional, nhưng bắt buộc nếu typeRole là SCHOOL |

## Lưu ý Quan Trọng

1. **Password Pattern**: Yêu cầu chữ hoa, chữ thường và số. Nếu muốn giảm độ khó, có thể bỏ pattern này và chỉ giữ `@Size`.

2. **Custom Validation**: Sử dụng `@AssertTrue` cho các validation phức tạp như `schoolId` phụ thuộc vào `scope` hoặc `typeRole`.

3. **Update DTOs**: Tất cả fields đều optional để cho phép partial update. Validation chỉ áp dụng khi field được cung cấp.

4. **Date Format**: `birthYear` sử dụng `LocalDateTime`. Frontend cần gửi format: `"1980-05-15T00:00:00"`.

5. **Pattern Validation**: 
   - `roleName` và `code`: Chỉ chữ hoa, số và dấu gạch dưới
   - `fullName` và `principalName`: Chỉ chữ cái và khoảng trắng (hỗ trợ Unicode)

## Cách Sử Dụng

### Trong Controller:
```java
@PostMapping
public ResponseEntity<?> createUser(@Valid @RequestBody UserRequest request) {
    // Validation tự động được thực hiện
}

@PutMapping("/{id}")
public ResponseEntity<?> updateUser(
    @PathVariable Long id,
    @Valid @RequestBody UpdateUserRequest request
) {
    // Chỉ validate các fields được cung cấp
}
```

### Test với Sample Data:
Xem file `sample-data/README.md` để biết cách sử dụng các file JSON mẫu.

