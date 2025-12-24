package com.backend.backend_crud.service.implement;

import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.entity.UserScope;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.exception.ValidationException;
import com.backend.backend_crud.mapper.RoleMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    // =================================================================
    // 1. LẤY DANH SÁCH ROLE (READ)
    // =================================================================
    @Override
    public ApiResponse<List<RoleResponse>> getAllRoles(RoleType typeRole, Long schoolId) {
        User currentUser = getCurrentUser();

        List<Role> roles;

        // TRƯỜNG HỢP 1: Lấy Role Hệ Thống (PROVIDER)
        if (typeRole == RoleType.PROVIDER) {
            // Chỉ nhân viên hệ thống mới được xem role hệ thống
            if (currentUser.getScope() != UserScope.PROVIDER) {
                throw new AppException.ForbiddenException("Bạn không có quyền truy cập Role hệ thống");
            }
            roles = roleRepository.findBySchoolIsNull(); // Hàm này DataSeeder dùng, ta tái sử dụng
        }
        // TRƯỜNG HỢP 2: Lấy Role Trường Học (SCHOOL)
        else if (typeRole == RoleType.SCHOOL) {
            if (currentUser.getScope() == UserScope.PROVIDER) {
                // Provider được xem:
                // a. Role mẫu (schoolId = null)
                // b. Role của một trường cụ thể (nếu truyền schoolId)
                if (schoolId == null) {
                    roles = roleRepository.findByTypeRoleAndSchoolIsNull(RoleType.SCHOOL);
                } else {
                    roles = roleRepository.findBySchoolId(schoolId);
                }
            } else {
                // Admin Trường: BẮT BUỘC chỉ được xem trường của mình
                Long userSchoolId = currentUser.getSchool().getId();

                // Nếu user cố tình truyền schoolId khác -> Chặn hoặc Force về school của họ
                if (schoolId != null && !schoolId.equals(userSchoolId)) {
                    throw new AppException.ForbiddenException("Bạn không thể xem dữ liệu của trường khác");
                }
                roles = roleRepository.findBySchoolId(userSchoolId);
            }
        } else {
            throw new AppException.BadRequestException("typeRole không hợp lệ");
        }

        return new ApiResponse<>(true, "Lấy danh sách thành công", mapRolesToResponses(roles));
    }

    @Override
    public ApiResponse<RoleResponse> getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role ID: " + id));

        // CHECK QUYỀN: User có được xem role này không?
        validateRoleAccess(getCurrentUser(), role);

        return new ApiResponse<>(true, "Thành công", toRoleResponse(role));
    }

    @Override
    public ApiResponse<RoleResponse> getRoleByName(String roleName, RoleType typeRole, Long schoolId) {
        // Tìm role theo logic cũ (để tương thích) nhưng thêm validate kỹ
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role: " + roleName));

        // Validate lại xem role tìm được có đúng ngữ cảnh school/type yêu cầu không
        if (role.getTypeRole() != typeRole) {
            throw new AppException.ResourceNotFoundException("Role tìm thấy không đúng loại yêu cầu");
        }

        // CHECK QUYỀN
        validateRoleAccess(getCurrentUser(), role);

        return new ApiResponse<>(true, "Thành công", toRoleResponse(role));
    }

    @Override
    public ApiResponse<List<RoleResponse>> searchRoles(String keyword, Long schoolId, RoleType typeRole) {
        User currentUser = getCurrentUser();

        // 1. Logic check quyền (Giữ nguyên)
        Long searchSchoolId = schoolId;
        if (currentUser.getScope() == UserScope.SCHOOL) {
            searchSchoolId = currentUser.getSchool().getId();
            if (typeRole == RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Không có quyền tìm kiếm Role hệ thống");
            }
        }

        // 2. Gọi Repository (Trả về List thay vì Page)
        List<Role> roles = roleRepository.searchRoles(searchSchoolId, keyword, typeRole);

        // 3. Map sang Response
        List<RoleResponse> roleResponses = roles.stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());

        return new ApiResponse<>(true, "Tìm kiếm thành công", roleResponses);
    }

    // =================================================================
    // 2. TẠO MỚI (CREATE) - DÙNG REPO MỚI
    // =================================================================
    @Override
    @Transactional
    public ApiResponse<RoleResponse> createRole(RoleResponse request, Long createBy) {
        User currentUser = getCurrentUser();

        // 1. Xác định ngữ cảnh (System hay School?)
        School school = null;
        if (request.getTypeRole() == RoleType.SCHOOL) {
            // Logic lấy schoolId chuẩn xác
            Long targetSchoolId;
            if (currentUser.getScope() == UserScope.SCHOOL) {
                targetSchoolId = currentUser.getSchool().getId(); // Admin trường -> Lấy ID trường của họ
            } else {
                targetSchoolId = request.getSchoolId(); // Provider -> Lấy ID trường họ nhập
                if (targetSchoolId == null) {
                    throw new AppException.BadRequestException("Cần nhập schoolId để tạo role cho trường");
                }
            }

            school = schoolRepository.findById(targetSchoolId)
                    .orElseThrow(() -> new AppException.ResourceNotFoundException("Trường học không tồn tại"));
        } else {
            // Tạo Role Provider
            if (currentUser.getScope() != UserScope.PROVIDER) {
                throw new AppException.ForbiddenException("Chỉ Admin hệ thống mới được tạo Role hệ thống");
            }
        }

        // 2. Validate Trùng Lặp (Dùng hàm REPO MỚI tối ưu)
        boolean isDuplicate = roleRepository.existsDuplicateRole(
                request.getRoleName(),
                request.getTypeRole(),
                school != null ? school.getId() : null);

        if (isDuplicate) {
            throw new ValidationException(Map.of("roleName", "Tên quyền hạn đã tồn tại trong phạm vi này"));
        }

        // 3. Map và Lưu
        Role role = roleMapper.mapToEntityFromResponse(request, school);
        role.setCreateBy(currentUser.getId());
        role.setUpdateBy(currentUser.getId());

        return new ApiResponse<>(true, "Tạo mới thành công", toRoleResponse(roleRepository.save(role)));
    }

    // =================================================================
    // 3. CẬP NHẬT (UPDATE) - DÙNG REPO MỚI
    // =================================================================
    @Override
    @Transactional
    public ApiResponse<RoleResponse> updateRole(Long id, RoleResponse request, Long updateBy) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role ID: " + id));

        User currentUser = getCurrentUser();

        // 1. CHECK QUYỀN: Có được sửa role này không?
        validateRoleAccess(currentUser, existingRole);

        // 2. Xử lý School (thường thì update role không cho đổi school, giữ nguyên cái
        // cũ)
        School school = existingRole.getSchool();

        // 3. Validate Trùng Lặp (Trừ chính nó ra) - Dùng hàm REPO MỚI
        if (request.getRoleName() != null && !request.getRoleName().equals(existingRole.getRoleName())) {
            boolean isDuplicate = roleRepository.existsDuplicateRoleForUpdate(
                    request.getRoleName(),
                    existingRole.getTypeRole(), // Thường không cho đổi TypeRole khi update
                    school != null ? school.getId() : null,
                    id);

            if (isDuplicate) {
                throw new ValidationException(Map.of("roleName", "Tên quyền hạn đã tồn tại"));
            }
        }

        // 4. Map và Lưu
        roleMapper.updateEntityFromResponse(existingRole, request, school);
        existingRole.setUpdateBy(currentUser.getId());

        return new ApiResponse<>(true, "Cập nhật thành công", toRoleResponse(roleRepository.save(existingRole)));
    }

    // =================================================================
    // 4. XÓA (DELETE)
    // =================================================================
    @Override
    @Transactional
    public ApiResponse<String> deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role ID: " + id));

        // CHECK QUYỀN
        User currentUser = getCurrentUser();
        validateRoleAccess(currentUser, role);

        // Check ràng buộc user
        Long count = userRepository.countByRoleId(id);
        if (count > 0) {
            throw new AppException.ConflictException(
                    "Không thể xóa role \"" + role.getRoleName() + "\". Đang có " + count
                            + " người dùng sử dụng role này. " +
                            "Vui lòng gán role mới cho các người dùng trước khi xóa.");
        }

        roleRepository.delete(role);
        return new ApiResponse<>(true, "Xóa thành công", "Đã xóa role \"" + role.getRoleName() + "\"");
    }

    @Override
    @Transactional
    public ApiResponse<String> reassignRoleAndDelete(Long oldRoleId, Long newRoleId) {
        Role oldRole = roleRepository.findById(oldRoleId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role cũ"));
        Role newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role mới"));

        User currentUser = getCurrentUser();

        // CHECK QUYỀN cho oldRole
        validateRoleAccess(currentUser, oldRole);

        // CHECK QUYỀN cho newRole - phải cùng phạm vi
        if (oldRole.getTypeRole() == RoleType.PROVIDER) {
            if (newRole.getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.BadRequestException("Role mới phải cùng loại (Hệ thống) với role cũ");
            }
        } else {
            if (newRole.getTypeRole() != RoleType.SCHOOL) {
                throw new AppException.BadRequestException("Role mới phải cùng loại (Trường học) với role cũ");
            }
            // Check cùng trường
            School oldSchool = oldRole.getSchool();
            School newSchool = newRole.getSchool();
            if (oldSchool != null && newSchool != null) {
                if (!oldSchool.getId().equals(newSchool.getId())) {
                    throw new AppException.BadRequestException("Role mới phải thuộc cùng trường với role cũ");
                }
            } else if (oldSchool != null || newSchool != null) {
                throw new AppException.BadRequestException("Role mới phải thuộc cùng trường với role cũ");
            }
        }

        // Lấy danh sách users đang sử dụng oldRole
        List<User> users = userRepository.findByRoleId(oldRoleId);
        if (users.isEmpty()) {
            // Nếu không có user nào, xóa trực tiếp
            roleRepository.delete(oldRole);
            return new ApiResponse<>(true, "Xóa thành công", "Đã xóa role \"" + oldRole.getRoleName() + "\"");
        }

        // Gán role mới cho tất cả users
        users.forEach(user -> {
            user.setRole(newRole);
            user.setUpdateBy(currentUser.getId());
            user.setUpdatedAt(java.time.LocalDateTime.now());
        });
        userRepository.saveAll(users);

        // Xóa role cũ
        roleRepository.delete(oldRole);

        return new ApiResponse<>(true, "Gán role mới và xóa role cũ thành công",
                "Đã gán role \"" + newRole.getRoleName() + "\" cho " + users.size() + " người dùng và xóa role \""
                        + oldRole.getRoleName() + "\"");
    }

    // =================================================================
    // PRIVATE HELPERS
    // =================================================================

    /**
     * Hàm check quyền cốt lõi:
     * - User Provider: Có thể làm mọi thứ (hoặc giới hạn tùy nghiệp vụ).
     * - User School:
     * + KHÔNG được đụng vào Role System (school=null).
     * + KHÔNG được đụng vào Role của School khác.
     */
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

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException.AuthenticationException("Vui lòng đăng nhập");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy thông tin người dùng"));
    }

    private RoleResponse toRoleResponse(Role role) {
        Long userCount = userRepository.countByRoleId(role.getId());
        return roleMapper.mapToResponse(role, userCount);
    }

    private List<RoleResponse> mapRolesToResponses(List<Role> roles) {
        if (roles == null || roles.isEmpty())
            return List.of();
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        Map<Long, Long> userCountsMap = userRepository.getUserCountsByRoleIds(roleIds); // Giả định Repo User có hàm này
        return roles.stream().map(this::toRoleResponse).collect(Collectors.toList());
    }
}