# ĐÁNH GIÁ MÃ NGUỒN JWT VÀ ROLE

**Ngày đánh giá:** 2024  
**Phạm vi:** JWT Authentication và Role Management

---

## 📊 TỔNG QUAN

### Kiến trúc JWT
- **JwtUtil**: Xử lý tạo và parse JWT token
- **JwtService**: Business logic cho JWT
- **JwtAuthenticationFilter**: Filter để validate token trong mỗi request
- **AuthService**: Xử lý login và tạo token

### Kiến trúc Role
- **RoleService/RoleServiceImpl**: Business logic quản lý role
- **RoleController**: REST API endpoints
- **RoleRepository**: Database operations với các query tối ưu

---

## ✅ ĐIỂM MẠNH

### 1. **JWT Implementation - Rất tốt** ⭐⭐⭐⭐⭐

#### **JwtService.java**
✅ **Điểm tốt:**
- Code clean, dễ đọc
- Đầy đủ methods: generateAccessToken, validateAccessToken, getUserIdFromToken, getEmailFromToken, getScopeFromToken, getRoleIdFromToken, getSchoolIdFromToken
- Error handling tốt với AppException.TokenException
- Claims được đóng gói đầy đủ: userId, email, scope, roleId, schoolId

```java
public String generateAccessToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("userId", user.getId());
    claims.put("email", user.getEmail());
    claims.put("scope", user.getScope().name());
    claims.put("roleId", user.getRole().getId());
    if (user.getSchool() != null) {
        claims.put("schoolId", user.getSchool().getId());
    }
    return jwtUtil.createToken(claims, user.getEmail(), jwtProperties.getExpiration());
}
```

#### **JwtUtil.java**
✅ **Điểm tốt:**
- Tối ưu với @PostConstruct để khởi tạo SecretKey một lần
- Helper methods hữu ích: extractLongClaim, extractStringClaim
- Error handling tốt

```java
@PostConstruct
private void initSigningKey() {
    byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
    this.signingKey = Keys.hmacShaKeyFor(keyBytes);
}
```

#### **JwtAuthenticationFilter.java**
✅ **Điểm tốt:**
- Xử lý token đúng cách
- Set authentication vào SecurityContext
- Có logging cho errors

#### **AuthService.java**
✅ **Điểm tốt:**
- Logic authentication rõ ràng
- Check isActive trước khi login
- Password validation
- Response format phù hợp với frontend

---

### 2. **Role Implementation - Đã cải thiện nhiều** ⭐⭐⭐⭐

#### **RoleServiceImpl.java**
✅ **Điểm tốt:**
- **Đã sửa:** Sử dụng `getCurrentUser()` từ SecurityContext thay vì JWT token trực tiếp
- **Đã sửa:** Có method `validateRoleAccess()` để check quyền tập trung
- **Đã sửa:** Logic phân quyền rõ ràng:
  - PROVIDER users: Có thể xem/tạo/sửa/xóa tất cả roles
  - SCHOOL users: Chỉ được xem/tạo/sửa/xóa roles của trường mình
- **Đã sửa:** Sử dụng repository methods mới: `existsDuplicateRole`, `searchRoles`
- **Đã sửa:** Có method `reassignRoleAndDelete()` để gán role mới và xóa role cũ
- Tối ưu N+1 query với batch query userCounts

```java
private void validateRoleAccess(User user, Role role) {
    // Nếu là Super Admin (Provider) -> Cho qua
    if (user.getScope() == UserScope.PROVIDER) {
        return;
    }
    // Nếu là Admin Trường
    if (user.getScope() == UserScope.SCHOOL) {
        // 1. Cấm sửa Role Hệ thống
        if (role.getSchool() == null || role.getTypeRole() == RoleType.PROVIDER) {
            throw new AppException.ForbiddenException("Bạn không có quyền thao tác trên Role hệ thống");
        }
        // 2. Cấm sửa Role trường khác
        if (!role.getSchool().getId().equals(user.getSchool().getId())) {
            throw new AppException.ForbiddenException("Bạn không có quyền thao tác trên dữ liệu của trường khác");
        }
    }
}
```

#### **RoleController.java**
✅ **Điểm tốt:**
- **Đã sửa:** Sử dụng `getCurrentUserId()` từ SecurityContext
- **Đã sửa:** Có @PreAuthorize ở class level
- **Đã sửa:** Có endpoint mới: `reassignRoleAndDelete`
- **Đã sửa:** Có endpoint search: `GET /api/roles/search`

```java
@PreAuthorize("hasAnyAuthority('SYSTEM_ADMIN', 'SCHOOL_ADMIN')")
public class RoleController {
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy thông tin người dùng"))
                .getId();
    }
}
```

#### **RoleRepository.java**
✅ **Điểm tốt:**
- **Đã thêm:** Method `existsDuplicateRole()` để check trùng lặp tối ưu
- **Đã thêm:** Method `existsDuplicateRoleForUpdate()` để check trùng lặp khi update
- **Đã thêm:** Method `searchRoles()` với query tối ưu
- **Đã thêm:** Method `findByTypeRoleAndSchoolIsNull()` để lấy role mẫu

```java
@Query("SELECT COUNT(r) > 0 FROM Role r WHERE " +
        "r.roleName = :roleName AND " +
        "r.typeRole = :typeRole AND " +
        "((:schoolId IS NULL AND r.school IS NULL) OR (r.school.id = :schoolId))")
boolean existsDuplicateRole(@Param("roleName") String roleName,
        @Param("typeRole") RoleType typeRole,
        @Param("schoolId") Long schoolId);
```

---

## ⚠️ VẤN ĐỀ CẦN SỬA

### 🔴 CRITICAL

#### 1. **INCONSISTENT: Hai cách lấy Current User**

**Vấn đề:**
- **UserController**: Lấy userId từ JWT token trực tiếp
- **RoleController, RoleServiceImpl, SchoolServiceImpl**: Lấy từ SecurityContext

**Code hiện tại:**

**UserController.java:**
```java
private Long getCurrentUserId() {
    String authHeader = httpServletRequest.getHeader("Authorization");
    String token = authHeader.substring(7);
    jwtService.validateAccessToken(token);
    return jwtService.getUserIdFromToken(token);  // ❌ Lấy từ JWT token
}
```

**RoleController.java:**
```java
private Long getCurrentUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userRepository.findByEmail(email)  // ✅ Lấy từ SecurityContext
            .orElseThrow(...)
            .getId();
}
```

**RoleServiceImpl.java:**
```java
private User getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String email = authentication.getName();
    return userRepository.findByEmail(email)  // ✅ Lấy từ SecurityContext
            .orElseThrow(...);
}
```

**Mức độ:** 🔴 CRITICAL  
**Vấn đề:**
- Inconsistent code, khó maintain
- UserController phải parse JWT token mỗi lần (không tối ưu)
- SecurityContext đã được set bởi JwtAuthenticationFilter, nên nên dùng SecurityContext

**Giải pháp:** Thống nhất dùng SecurityContext cho tất cả controllers

---

#### 2. **SECURITY: JWT Secret Hardcoded**

**File:** `JwtProperties.java`, `application.properties`

**Vấn đề:**
```java
private String secret = "your-secret-key-change-this-in-production...";
```

```properties
jwt.secret=your-secret-key-change-this-in-production-to-a-long-random-secure-string-at-least-256-bits-minimum-32-characters
```

**Mức độ:** 🔴 CRITICAL  
**Giải pháp:** Sử dụng environment variables

---

### 🟡 HIGH

#### 3. **PERFORMANCE: UserController query DB mỗi lần**

**Vấn đề:**
UserController lấy userId từ JWT token, nhưng nếu cần thông tin user đầy đủ thì phải query DB thêm.

**Giải pháp:** Nên dùng SecurityContext như RoleController, vì JwtAuthenticationFilter đã load user vào SecurityContext.

---

#### 4. **CODE DUPLICATION: getCurrentUserId/getCurrentUser**

**Vấn đề:**
- UserController có `getCurrentUserId()`
- RoleController có `getCurrentUserId()`
- RoleServiceImpl có `getCurrentUser()`
- SchoolServiceImpl có `getCurrentUser()`

**Giải pháp:** Tạo một utility class hoặc base controller

---

### 🟢 MEDIUM

#### 5. **MISSING: Refresh Token**

**Vấn đề:** Chỉ có access token, không có refresh token

**Giải pháp:** Có thể thêm refresh token mechanism

---

#### 6. **MISSING: Token Blacklist**

**Vấn đề:** Không có cơ chế blacklist token khi logout

**Giải pháp:** Có thể thêm Redis để lưu blacklist

---

## 📊 SO SÁNH TRƯỚC VÀ SAU

### **Trước khi sửa:**
- ❌ RoleServiceImpl: Lấy userId từ JWT token trực tiếp
- ❌ RoleController: Lấy userId từ JWT token trực tiếp
- ❌ Không có validateRoleAccess() tập trung
- ❌ Logic phân quyền rải rác

### **Sau khi sửa:**
- ✅ RoleServiceImpl: Lấy từ SecurityContext
- ✅ RoleController: Lấy từ SecurityContext
- ✅ Có validateRoleAccess() tập trung
- ✅ Logic phân quyền rõ ràng
- ✅ Có các repository methods mới tối ưu
- ✅ Có endpoint reassignRoleAndDelete
- ✅ Có endpoint search

---

## 🎯 KHUYẾN NGHỊ

### **PRIORITY 0 - CRITICAL**
1. ✅ **Thống nhất cách lấy Current User:**
   - Sửa UserController để dùng SecurityContext như RoleController
   - Tạo utility method hoặc base controller

2. ✅ **Di chuyển JWT secret ra environment variables**

### **PRIORITY 1 - HIGH**
3. ✅ **Tạo utility class cho getCurrentUser/getCurrentUserId:**
   ```java
   @Component
   public class SecurityUtils {
       public static User getCurrentUser() { ... }
       public static Long getCurrentUserId() { ... }
   }
   ```

### **PRIORITY 2 - MEDIUM**
4. ✅ **Thêm refresh token mechanism**
5. ✅ **Thêm token blacklist cho logout**

---

## 📈 ĐIỂM TỔNG THỂ

| Module | Điểm | Ghi chú |
|--------|------|---------|
| **JWT Service** | 9/10 | Rất tốt, chỉ thiếu refresh token |
| **JWT Util** | 9/10 | Tối ưu, code clean |
| **JWT Filter** | 9/10 | Xử lý đúng cách |
| **Auth Service** | 9/10 | Logic rõ ràng |
| **Role Service** | 8/10 | Đã cải thiện nhiều, còn inconsistent với UserController |
| **Role Controller** | 8/10 | Đã sửa tốt, có @PreAuthorize |
| **Role Repository** | 9/10 | Có các methods tối ưu |

**TỔNG ĐIỂM JWT: 9.0/10** ⭐⭐⭐⭐⭐  
**TỔNG ĐIỂM ROLE: 8.3/10** ⭐⭐⭐⭐

---

## ✅ KẾT LUẬN

### **JWT Implementation: RẤT TỐT** ⭐⭐⭐⭐⭐
- Code quality cao
- Error handling tốt
- Tối ưu performance
- Chỉ cần thêm refresh token và blacklist

### **Role Implementation: TỐT, ĐÃ CẢI THIỆN NHIỀU** ⭐⭐⭐⭐
- Đã sửa để dùng SecurityContext
- Có validateRoleAccess() tập trung
- Logic phân quyền rõ ràng
- Có các methods mới tối ưu
- **Còn vấn đề:** Inconsistent với UserController

### **Cần sửa ngay:**
1. Thống nhất cách lấy Current User (UserController → SecurityContext)
2. Di chuyển JWT secret ra environment variables

**Sau khi sửa 2 vấn đề trên, code sẽ rất tốt và sẵn sàng cho production!**

---

## 📝 CHECKLIST

- [x] RoleServiceImpl đã dùng SecurityContext
- [x] RoleController đã dùng SecurityContext
- [x] Có validateRoleAccess() tập trung
- [x] Có các repository methods tối ưu
- [ ] UserController cần sửa để dùng SecurityContext
- [ ] JWT secret cần di chuyển ra environment variables
- [ ] Tạo utility class cho getCurrentUser
- [ ] Thêm refresh token (optional)
- [ ] Thêm token blacklist (optional)

---

**Ngày đánh giá:** 2024  
**Đánh giá tổng thể: TỐT, CẦN SỬA 2 VẤN ĐỀ CRITICAL**




