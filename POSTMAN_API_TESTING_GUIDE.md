# HƯỚNG DẪN TEST API VỚI POSTMAN

## 📋 MỤC LỤC
1. [Thiết lập môi trường](#thiết-lập-môi-trường)
2. [Authentication](#authentication)
3. [User APIs](#user-apis)
4. [Role APIs](#role-apis)
5. [School APIs](#school-apis)
6. [Collection Variables](#collection-variables)

---

## 🔧 THIẾT LẬP MÔI TRƯỜNG

### Base URL
```
http://localhost:8080
```

### Headers mặc định
Tất cả các request (trừ login) cần có header:
```
Authorization: Bearer {token}
Content-Type: application/json
```

### Tạo Environment trong Postman
1. Click vào **Environments** → **+**
2. Tạo environment mới với tên: `Backend CRUD Local`
3. Thêm các variables:
   - `base_url`: `http://localhost:8080`
   - `token`: (để trống, sẽ được set sau khi login)

---

## 🔐 AUTHENTICATION

### 1. Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "email": "admin@system.com",
  "password": "Admin123"
}
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Đăng nhập thành công",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "user": {
      "id": 1,
      "email": "admin@system.com",
      "fullName": "Admin Hệ Thống",
      "scope": "PROVIDER",
      "schoolId": null,
      "roleId": 1
    }
  }
}
```

**Postman Setup:**
1. Tạo request: `POST {{base_url}}/api/auth/login`
2. Body → raw → JSON
3. Sau khi nhận response, thêm Test Script:
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("token", jsonData.data.token);
    pm.environment.set("user_id", jsonData.data.user.id);
    pm.environment.set("user_email", jsonData.data.user.email);
}
```

**Test Accounts (từ DataSeeder):**
- **SYSTEM_ADMIN**: `admin@system.com` / `Admin123`
- **SYSTEM_STAFF**: `staff@system.com` / `Staff123`
- **SCHOOL_ADMIN (School 1)**: `an.nguyen@school1.edu.vn` / `Admin123`
- **TEACHER (School 1)**: `lan.tran@school1.edu.vn` / `Teacher123`
- **STUDENT (School 1)**: `hung.le@school1.edu.vn` / `Student123`

---

## 👥 USER APIs

### 1. Lấy danh sách Users
**Endpoint:** `GET /api/users`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Lấy danh sách User thành công",
  "data": [
    {
      "id": 1,
      "fullName": "Admin Hệ Thống",
      "email": "admin@system.com",
      "gender": "MALE",
      "birthYear": "1980-05-15T00:00:00",
      "address": "10 Đường Trần Phú, Hà Nội",
      "phoneNumber": "0912345678",
      "isActive": true,
      "scope": "PROVIDER",
      "schoolId": null,
      "schoolName": null,
      "roleId": 1,
      "roleName": "SYSTEM_ADMIN",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ]
}
```

**Lưu ý:**
- SYSTEM_ADMIN: Xem tất cả users hệ thống (scope = PROVIDER)
- SCHOOL_ADMIN: Chỉ xem users của trường mình (scope = SCHOOL)

---

### 2. Tìm kiếm Users
**Endpoint:** `GET /api/users/search?keyword={keyword}`

**Query Parameters:**
- `keyword` (optional): Từ khóa tìm kiếm theo tên

**Example:**
```
GET /api/users/search?keyword=Nguyễn
```

**Response:** Tương tự như GET /api/users

---

### 3. Tạo User mới
**Endpoint:** `POST /api/users`

**Headers:**
```
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "fullName": "Nguyễn Văn A",
  "gender": "MALE",
  "birthYear": "1990-01-15T00:00:00",
  "address": "123 Đường ABC, Hà Nội",
  "phoneNumber": "0912345678",
  "email": "nguyenvana@example.com",
  "password": "Password123",
  "scope": "PROVIDER",
  "isActive": true,
  "schoolId": null,
  "roleId": 1
}
```

**Validation Rules:**
- `fullName`: 2-100 ký tự, không được để trống
- `email`: Email hợp lệ, không được để trống
- `password`: 6-100 ký tự, không được để trống
- `phoneNumber`: Format `0xxxxxxxxx` hoặc `+84xxxxxxxxx`
- `birthYear`: LocalDateTime, phải là ngày trong quá khứ
- `scope`: PROVIDER hoặc SCHOOL
- `roleId`: Bắt buộc

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Tạo mới User thành công",
  "data": {
    "id": 10,
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@example.com",
    ...
  }
}
```

**Lưu ý:**
- SYSTEM_ADMIN: Chỉ tạo được users với scope = PROVIDER
- SCHOOL_ADMIN: Chỉ tạo được users với scope = SCHOOL và schoolId phải trùng với trường của mình

---

### 4. Cập nhật User
**Endpoint:** `PUT /api/users/{id}`

**Path Parameters:**
- `id`: ID của user cần cập nhật

**Request Body:** Tương tự như tạo user

**Example:**
```
PUT /api/users/1
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Cập nhật User thành công",
  "data": { ... }
}
```

---

### 5. Xóa User
**Endpoint:** `DELETE /api/users/{id}`

**Path Parameters:**
- `id`: ID của user cần xóa

**Example:**
```
DELETE /api/users/5
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Xóa User thành công",
  "data": null
}
```

---

### 6. Lấy Users theo Role ID
**Endpoint:** `GET /api/users/by-role/{roleId}`

**Path Parameters:**
- `roleId`: ID của role

**Example:**
```
GET /api/users/by-role/1
```

**Response:** Danh sách users có roleId tương ứng

---

### 7. Kiểm tra Role đang được sử dụng
**Endpoint:** `GET /api/users/role-in-use/{roleId}`

**Path Parameters:**
- `roleId`: ID của role

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Kiểm tra thành công",
  "data": true
}
```

---

### 8. Gán lại Role cho Users
**Endpoint:** `PUT /api/users/reassign-role?oldRoleId={oldRoleId}&newRoleId={newRoleId}`

**Query Parameters:**
- `oldRoleId`: ID của role cũ
- `newRoleId`: ID của role mới

**Example:**
```
PUT /api/users/reassign-role?oldRoleId=5&newRoleId=6
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Đã gán role mới cho 10 người dùng",
  "data": "10"
}
```

**Lưu ý:**
- Role mới phải cùng typeRole với role cũ
- Nếu là SCHOOL role, phải cùng school

---

## 🎭 ROLE APIs

### 1. Lấy danh sách Roles
**Endpoint:** `GET /api/roles?typeRole={typeRole}&schoolId={schoolId}`

**Query Parameters:**
- `typeRole` (required): `PROVIDER` hoặc `SCHOOL`
- `schoolId` (required nếu typeRole = SCHOOL): ID của trường

**Examples:**
```
GET /api/roles?typeRole=PROVIDER
GET /api/roles?typeRole=SCHOOL&schoolId=1
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Lấy danh sách thành công",
  "data": [
    {
      "id": 1,
      "roleName": "SYSTEM_ADMIN",
      "typeRole": "PROVIDER",
      "description": "Quản trị viên hệ thống",
      "schoolId": null,
      "schoolName": null,
      "userCount": 5,
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ]
}
```

**Lưu ý:**
- PROVIDER roles: Chỉ SYSTEM_ADMIN mới xem được
- SCHOOL roles: Chỉ SCHOOL_ADMIN của trường đó mới xem được

---

### 2. Lấy Role theo ID
**Endpoint:** `GET /api/roles/{id}`

**Path Parameters:**
- `id`: ID của role

**Example:**
```
GET /api/roles/1
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Lấy thông tin role thành công",
  "data": {
    "id": 1,
    "roleName": "SYSTEM_ADMIN",
    ...
  }
}
```

---

### 3. Lấy Role theo tên
**Endpoint:** `GET /api/roles/name/{roleName}?typeRole={typeRole}&schoolId={schoolId}`

**Path Parameters:**
- `roleName`: Tên của role (ví dụ: SYSTEM_ADMIN)

**Query Parameters:**
- `typeRole` (required): `PROVIDER` hoặc `SCHOOL`
- `schoolId` (required nếu typeRole = SCHOOL)

**Example:**
```
GET /api/roles/name/SYSTEM_ADMIN?typeRole=PROVIDER
GET /api/roles/name/SCHOOL_ADMIN?typeRole=SCHOOL&schoolId=1
```

---

### 4. Tạo Role mới
**Endpoint:** `POST /api/roles`

**Request Body:**
```json
{
  "roleName": "NEW_ROLE",
  "typeRole": "PROVIDER",
  "description": "Mô tả role mới",
  "schoolId": null
}
```

**Validation Rules:**
- `roleName`: 2-50 ký tự, chỉ chữ hoa, số và dấu gạch dưới (pattern: `^[A-Z0-9_]+$`)
- `typeRole`: PROVIDER hoặc SCHOOL
- `description`: 5-500 ký tự
- `schoolId`: Bắt buộc nếu typeRole = SCHOOL

**Response (201 Created):**
```json
{
  "status": true,
  "message": "Tạo thành công",
  "data": {
    "id": 10,
    "roleName": "NEW_ROLE",
    ...
  }
}
```

**Lưu ý:**
- SYSTEM_ADMIN: Chỉ tạo được PROVIDER roles
- SCHOOL_ADMIN: Chỉ tạo được SCHOOL roles cho trường của mình

---

### 5. Cập nhật Role
**Endpoint:** `PUT /api/roles/{id}`

**Path Parameters:**
- `id`: ID của role cần cập nhật

**Request Body:**
```json
{
  "roleName": "UPDATED_ROLE",
  "typeRole": "PROVIDER",
  "description": "Mô tả đã cập nhật",
  "schoolId": null
}
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Cập nhật thành công",
  "data": { ... }
}
```

---

### 6. Xóa Role
**Endpoint:** `DELETE /api/roles/{id}`

**Path Parameters:**
- `id`: ID của role cần xóa

**Example:**
```
DELETE /api/roles/5
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Xóa thành công",
  "data": "ID: 5"
}
```

**Lưu ý:**
- Không thể xóa role nếu đang có users sử dụng
- Phải gán role khác cho users trước khi xóa

---

## 🏫 SCHOOL APIs

**Lưu ý:** Tất cả School APIs yêu cầu quyền `SYSTEM_ADMIN`

### 1. Lấy danh sách Schools
**Endpoint:** `GET /api/schools`

**Headers:**
```
Authorization: Bearer {{token}}
```

**Response (200 OK):**
```json
{
  "status": true,
  "message": "Lấy danh sách trường học thành công",
  "data": [
    {
      "id": 1,
      "name": "Trường Tiểu học Nguyễn Du",
      "code": "TH001",
      "email": "thnguyendu@edu.vn",
      "hotline": "0241234567",
      "address": "123 Đường Nguyễn Du, Quận Hoàn Kiếm, Hà Nội",
      "principalName": "Nguyễn Văn An",
      "createdAt": "2024-01-01T00:00:00",
      "updatedAt": "2024-01-01T00:00:00"
    }
  ]
}
```

---

### 2. Lấy School theo ID
**Endpoint:** `GET /api/schools/{id}`

**Path Parameters:**
- `id`: ID của school

**Example:**
```
GET /api/schools/1
```

---

### 3. Tạo School mới
**Endpoint:** `POST /api/schools`

**Request Body:**
```json
{
  "name": "Trường THCS Mới",
  "code": "THCS999",
  "email": "thcsmoi@edu.vn",
  "hotline": "0249999999",
  "address": "999 Đường Mới, Hà Nội",
  "principalName": "Nguyễn Văn Mới"
}
```

**Validation Rules:**
- `name`: 2-200 ký tự, chỉ chữ cái, số, khoảng trắng và ký tự đặc biệt cơ bản
- `code`: 2-50 ký tự, chỉ chữ hoa, số và dấu gạch dưới
- `email`: Email hợp lệ, tối đa 100 ký tự
- `hotline`: Format `0xxxxxxxxx` hoặc `+84xxxxxxxxx`
- `address`: 5-255 ký tự
- `principalName`: 2-100 ký tự, chỉ chữ cái và khoảng trắng

**Response (201 Created):**
```json
{
  "status": true,
  "message": "Tạo trường học mới thành công",
  "data": {
    "id": 4,
    "name": "Trường THCS Mới",
    ...
  }
}
```

**Lưu ý:** Khi tạo school, hệ thống sẽ tự động:
1. Tạo role `SCHOOL_ADMIN` cho school đó
2. Tạo user hiệu trưởng với email = school.email, password = "12345678"

---

### 4. Cập nhật School
**Endpoint:** `PUT /api/schools/{id}`

**Path Parameters:**
- `id`: ID của school cần cập nhật

**Request Body:** Tương tự như tạo school

---

### 5. Xóa School
**Endpoint:** `DELETE /api/schools/{id}`

**Path Parameters:**
- `id`: ID của school cần xóa

**Lưu ý:** Khi xóa school, tất cả users và roles của school đó cũng sẽ bị xóa (CASCADE)

---

### 6. Tìm kiếm Schools
**Endpoint:** `GET /api/schools/search?name={name}`

**Query Parameters:**
- `name`: Tên trường cần tìm

**Example:**
```
GET /api/schools/search?name=Nguyễn
```

---

## 📝 COLLECTION VARIABLES

### Tạo Collection trong Postman
1. Tạo Collection mới: `Backend CRUD API`
2. Thêm Pre-request Script cho Collection:
```javascript
// Auto-set Authorization header nếu có token
if (pm.environment.get("token")) {
    pm.request.headers.add({
        key: "Authorization",
        value: "Bearer " + pm.environment.get("token")
    });
}
```

### Environment Variables
- `base_url`: `http://localhost:8080`
- `token`: JWT token (set sau khi login)
- `user_id`: ID của user đang đăng nhập
- `user_email`: Email của user đang đăng nhập

---

## 🧪 TEST SCENARIOS

### Scenario 1: Flow đầy đủ cho SYSTEM_ADMIN
1. **Login** với `admin@system.com` / `Admin123`
2. **Tạo School mới** → Lưu `school_id`
3. **Lấy Roles PROVIDER** → Lưu `provider_role_id`
4. **Tạo User PROVIDER** với role vừa lấy
5. **Lấy danh sách Users** → Verify user mới tạo
6. **Cập nhật User** → Verify thay đổi
7. **Xóa User** → Verify đã xóa

### Scenario 2: Flow cho SCHOOL_ADMIN
1. **Login** với `an.nguyen@school1.edu.vn` / `Admin123`
2. **Lấy Roles SCHOOL** với `schoolId=1`
3. **Tạo User SCHOOL** với `schoolId=1`
4. **Tìm kiếm Users** theo keyword
5. **Lấy Users theo Role ID**

### Scenario 3: Test Role Management
1. **Login** với SYSTEM_ADMIN
2. **Tạo Role PROVIDER mới**
3. **Kiểm tra Role đang được sử dụng** → Should be false
4. **Gán Role cho User**
5. **Kiểm tra lại** → Should be true
6. **Thử xóa Role** → Should fail (có users đang dùng)
7. **Gán lại Role khác cho users**
8. **Xóa Role** → Should success

---

## ⚠️ ERROR RESPONSES

### 401 Unauthorized
```json
{
  "status": false,
  "message": "Token không hợp lệ hoặc đã hết hạn. Vui lòng đăng nhập lại.",
  "data": null
}
```

### 403 Forbidden
```json
{
  "status": false,
  "message": "Bạn không có quyền truy cập tài nguyên này",
  "data": null
}
```

### 404 Not Found
```json
{
  "status": false,
  "message": "Không tìm thấy role với id: 999",
  "data": null
}
```

### 400 Bad Request (Validation Error)
```json
{
  "status": false,
  "message": "Dữ liệu không hợp lệ",
  "data": {
    "email": "Email không hợp lệ",
    "fullName": "Họ tên không được để trống"
  }
}
```

### 409 Conflict
```json
{
  "status": false,
  "message": "Email đã tồn tại trong hệ thống",
  "data": null
}
```

---

## 📦 POSTMAN COLLECTION EXPORT

Để export collection:
1. Click vào Collection → **...** → **Export**
2. Chọn format: **Collection v2.1**
3. Lưu file: `Backend_CRUD_API.postman_collection.json`

---

## 🔍 TIPS & TRICKS

1. **Auto-save token**: Sử dụng Test Script trong login request để tự động lưu token
2. **Environment switching**: Tạo nhiều environments cho dev/staging/prod
3. **Pre-request Script**: Tự động thêm Authorization header cho tất cả requests
4. **Test Scripts**: Verify response status và data structure
5. **Variables**: Sử dụng variables để dễ dàng thay đổi giá trị

---

## 📚 THAM KHẢO

- **Base URL**: `http://localhost:8080`
- **API Prefix**: `/api`
- **Authentication**: JWT Bearer Token
- **Content-Type**: `application/json`

---

**Chúc bạn test thành công! 🚀**

