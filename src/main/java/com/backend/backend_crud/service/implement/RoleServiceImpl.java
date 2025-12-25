package com.backend.backend_crud.service.implement;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.request.UpdateRoleRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.PageResponse;
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
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    // =================================================================
    // 1. LẤY DANH SÁCH ROLE (READ)
    // =================================================================
    @Override
    public ApiResponse<PageResponse<RoleResponse>> getAllRoles(RoleType typeRole, int page, int size) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        // 1. Tạo Pageable
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Role> pageData;

        if (currentUser.getScope() == UserScope.PROVIDER) {
            // Check quyền: Admin hệ thống chỉ được xem role hệ thống
            if (typeRole != null && typeRole != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Admin hệ thống không được phép xem danh sách Role của trường học");
            }
            // Gọi Repository lấy Page
            pageData = roleRepository.findBySchoolIsNull(RoleType.PROVIDER, pageable);
        }
        else {
            // Check quyền: Admin trường chỉ được xem role trường
            if (typeRole != null && typeRole != RoleType.SCHOOL) {
                throw new AppException.ForbiddenException("Admin trường không được phép xem Role hệ thống");
            }

            // Gọi Repository lấy Page theo SchoolId
            pageData = roleRepository.findBySchoolId(currentUser.getSchool().getId(), pageable);
        }

        return new ApiResponse<>(true, "Lấy danh sách thành công", mapRolePageToResponse(pageData));


    }

    @Override
    public ApiResponse<RoleResponse> getRoleById(Long id) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role ID: " + id));

        // CHỐT CHẶN 2: Check quyền sở hữu (Isolation)
        validateRoleAccess(currentUser, role);

        return new ApiResponse<>(true, "Thành công", toRoleResponse(role));
    }

    @Override
    public ApiResponse<RoleResponse> getRoleByName(String roleName, RoleType typeRole, Long schoolId) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role: " + roleName));

        validateRoleAccess(currentUser, role);

        if (role.getTypeRole() != typeRole) {
            throw new AppException.ResourceNotFoundException("Role tìm thấy không đúng loại yêu cầu");
        }
        return new ApiResponse<>(true, "Thành công", toRoleResponse(role));
    }

    @Override
    public ApiResponse<PageResponse<RoleResponse>> searchRoles(String keyword, Long schoolId, RoleType typeRole, int page, int size) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        // 1. Logic check quyền (Giữ nguyên)
        Long searchSchoolId = schoolId;
        if (currentUser.getScope() == UserScope.SCHOOL) {
            searchSchoolId = currentUser.getSchool().getId();
            if (typeRole == RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Không có quyền tìm kiếm Role hệ thống");
            }
        }

        // 2. Tạo Pageable (Tham số thứ 4 còn thiếu)
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // 3. Gọi Repository (Truyền đủ 4 tham số)
        // Lưu ý: searchRoles trả về Page<Role>, không phải List<Role>
        Page<Role> pageData = roleRepository.searchRoles(searchSchoolId, keyword, typeRole, pageable);

        // 4. Map sang PageResponse (Dùng hàm helper đã viết ở bước trước)
        return new ApiResponse<>(true, "Tìm kiếm thành công", mapRolePageToResponse(pageData));
    }

    // =================================================================
    // 2. TẠO MỚI (CREATE) - DÙNG REPO MỚI
    // =================================================================
    @Override
    @Transactional
    public ApiResponse<RoleResponse> createRole(RoleRequest request) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        School school = determineSchoolForCreate(currentUser, request.getTypeRole());

        // 2. Validate Trùng Lặp (Dùng hàm REPO MỚI tối ưu)
        boolean isDuplicate = roleRepository.existsDuplicateRole(
                request.getRoleName(),
                request.getTypeRole(),
                school != null ? school.getId() : null);

        if (isDuplicate) {
            throw new ValidationException(Map.of("roleName", "Tên quyền hạn đã tồn tại trong phạm vi này"));
        }

        Role role = roleMapper.mapToEntity(request, school);
        role.setCreateBy(currentUser.getId());
        role.setUpdateBy(currentUser.getId());

        //Long userCount = userRepository.countByRoleId(role.getId());
        //return roleMapper.mapToResponse(role, userCount);
        return new ApiResponse<>(true, "Tạo mới thành công", toRoleResponse(roleRepository.save(role)));
    }

    // =================================================================
    // 3. CẬP NHẬT (UPDATE) - DÙNG REPO MỚI
    // =================================================================
    @Override
    @Transactional
    public ApiResponse<RoleResponse> updateRole(Long id, UpdateRoleRequest request) {
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role ID: " + id));

        // Check quyền sở hữu
        validateRoleAccess(currentUser, existingRole);

        School school = existingRole.getSchool();

        // Validate trùng tên
        if (request.getRoleName() != null && !request.getRoleName().equals(existingRole.getRoleName())) {
            boolean isDuplicate = roleRepository.existsDuplicateRoleForUpdate(
                    request.getRoleName(),
                    existingRole.getTypeRole(),
                    school != null ? school.getId() : null,
                    id);

            if (isDuplicate) {
                throw new ValidationException(Map.of("roleName", "Tên quyền hạn đã tồn tại"));
            }
        }
        roleMapper.updateEntityFromRequest(existingRole, request);
//        // Cập nhật
//        existingRole.setRoleName(request.getRoleName());
//        existingRole.setDescription(request.getDescription());
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
        ensureAdminAccess(currentUser);
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
        User currentUser = getCurrentUser();
        ensureAdminAccess(currentUser);

        Role oldRole = roleRepository.findById(oldRoleId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role cũ"));
        Role newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role mới"));

        // CHECK QUYỀN cho oldRole, newRole
        validateRoleAccess(currentUser, oldRole);
        validateRoleAccess(currentUser, newRole);

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
    private void ensureAdminAccess(User user) {
        String roleName = user.getRole().getRoleName();

        // Nếu là user Hệ thống -> Bắt buộc phải là SYSTEM_ADMIN
        if (user.getScope() == UserScope.PROVIDER) {
            if (!"SYSTEM_ADMIN".equals(roleName)) {
                throw new AppException.ForbiddenException("Truy cập bị từ chối: Chỉ SYSTEM_ADMIN mới được phép thao tác Role.");
            }
        }
        // Nếu là user Trường học -> Bắt buộc phải là SCHOOL_ADMIN
        else if (user.getScope() == UserScope.SCHOOL) {
            if (!"SCHOOL_ADMIN".equals(roleName)) {
                throw new AppException.ForbiddenException("Truy cập bị từ chối: Chỉ SCHOOL_ADMIN mới được phép thao tác Role.");
            }
        }
    }

    private void validateRoleAccess(User user, Role role) {
        // Admin Hệ thống -> Chỉ được chạm vào PROVIDER Role
        if (user.getScope() == UserScope.PROVIDER) {
            if (role.getTypeRole() == RoleType.SCHOOL || role.getSchool() != null) {
                throw new AppException.ForbiddenException("Admin hệ thống không được phép thao tác dữ liệu của trường học.");
            }
        }
        // Admin Trường -> Chỉ được chạm vào SCHOOL Role CỦA MÌNH
        else if (user.getScope() == UserScope.SCHOOL) {
            if (role.getTypeRole() == RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Admin trường không được phép thao tác Role hệ thống.");
            }
            if (role.getSchool() == null || !role.getSchool().getId().equals(user.getSchool().getId())) {
                throw new AppException.ForbiddenException("Bạn không có quyền thao tác dữ liệu của trường khác.");
            }
        }
    }

    private School determineSchoolForCreate(User user, RoleType requestType) {
        if (user.getScope() == UserScope.PROVIDER) {
            if (requestType != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Admin hệ thống KHÔNG ĐƯỢC tạo role trường học");
            }
            return null; // System Role luôn có school = null
        } else {
            // UserScope.SCHOOL (Đã qua hàm ensureAdminAccess nên chắc chắn là School Admin)
            if (requestType != RoleType.SCHOOL) {
                throw new AppException.ForbiddenException("Admin trường KHÔNG ĐƯỢC tạo role hệ thống");
            }
            return user.getSchool(); // School Role luôn gắn với trường của Admin đó
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
        if (roles == null || roles.isEmpty()) return List.of();
        return roles.stream().map(this::toRoleResponse).collect(Collectors.toList());
    }
    private PageResponse<RoleResponse> mapRolePageToResponse(Page<Role> pageData) {
        // 1. Convert list entity sang list DTO
        List<RoleResponse> responseList = pageData.getContent().stream()
                .map(this::toRoleResponse)
                .collect(Collectors.toList());

        // 2. Build PageResponse

        return PageResponse.<RoleResponse>builder()
                .data(responseList) // <--- SỬA CHỖ NÀY: Thử đổi 'content' thành 'data'
                .page(pageData.getNumber() + 1)
                .size(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .totalPages(pageData.getTotalPages())
                .build();
    }
}