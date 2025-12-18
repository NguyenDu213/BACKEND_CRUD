# Sample Data cho Testing API

Các file JSON này chứa dữ liệu mẫu để test các API endpoints.

## Cấu trúc

### user-requests.json
- `createUserRequests`: Danh sách các request để tạo user mới
- `updateUserRequests`: Danh sách các request để cập nhật user (các trường optional)

### school-requests.json
- `createSchoolRequests`: Danh sách các request để tạo school mới
- `updateSchoolRequests`: Danh sách các request để cập nhật school (các trường optional)

### role-requests.json
- `createRoleRequests`: Danh sách các request để tạo role mới
- `updateRoleRequests`: Danh sách các request để cập nhật role (các trường optional)

## Cách sử dụng

### Test với Postman/Insomnia:
1. Import các file JSON này
2. Sử dụng các request mẫu để test API endpoints
3. Điều chỉnh dữ liệu theo nhu cầu test

### Test với cURL:
```bash
# Tạo user mới
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d @sample-data/user-requests.json

# Cập nhật user
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Nguyễn Văn An Updated",
    "phoneNumber": "0911111111"
  }'
```

## Lưu ý

1. **birthYear**: Format là `LocalDateTime` (ví dụ: "1980-05-15T00:00:00")
2. **password**: Phải tuân thủ pattern: ít nhất 1 chữ hoa, 1 chữ thường, 1 số
3. **phoneNumber**: Format: `0xxxxxxxxx` hoặc `+84xxxxxxxxx`
4. **email**: Phải đúng format email hợp lệ
5. **schoolId**: Bắt buộc khi `scope` là `SCHOOL` hoặc `typeRole` là `SCHOOL`
6. **roleName**: Chỉ chứa chữ hoa, số và dấu gạch dưới (ví dụ: `SYSTEM_ADMIN`)

## Validation Rules

### UserRequest
- `fullName`: 2-100 ký tự, chỉ chữ cái và khoảng trắng
- `birthYear`: Phải là ngày trong quá khứ
- `address`: 5-255 ký tự
- `phoneNumber`: Format `^(0|\\+84)[0-9]{9,10}$`
- `email`: Format email hợp lệ, tối đa 100 ký tự
- `password`: 6-100 ký tự, phải có chữ hoa, chữ thường và số
- `schoolId`: Bắt buộc nếu `scope` là `SCHOOL`

### SchoolRequest
- `name`: 2-200 ký tự
- `code`: 2-50 ký tự, chỉ chữ hoa, số và dấu gạch dưới
- `email`: Format email hợp lệ, tối đa 100 ký tự
- `hotline`: Format `^(0|\\+84)[0-9]{9,10}$`
- `address`: 5-255 ký tự
- `principalName`: 2-100 ký tự, chỉ chữ cái và khoảng trắng

### RoleRequest
- `roleName`: 2-50 ký tự, chỉ chữ hoa, số và dấu gạch dưới
- `description`: 5-500 ký tự
- `schoolId`: Bắt buộc nếu `typeRole` là `SCHOOL`

