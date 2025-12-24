package com.backend.backend_crud.service.implement;

import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.exception.ValidationException;
import com.backend.backend_crud.mapper.RoleMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService{

    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
//    private final JwtService jwtService;
//    private final HttpServletRequest httpServletRequest;

    // ================= PUBLIC METHODS =================

    @Override
    public ApiResponse<List<RoleResponse>> getAllRoles(RoleType typeRole, Long schoolId) {
        User currentUser = getCurrentUser();

        if (typeRole == RoleType.PROVIDER) {
            validateProviderAccess(currentUser);
            List<Role> roles = roleRepository.findBySchoolIsNull();
            return new ApiResponse<>(true, "Lấy danh sách role hệ thống thành công", mapRolesToResponses(roles));
        }

        if (typeRole == RoleType.SCHOOL) {
            // Logic: Provider được xem danh sách role School (cấp hệ thống hoặc của trường)
            if (currentUser.getRole().getTypeRole() == RoleType.PROVIDER) {
                // Lấy các role SCHOOL mẫu (schoolId = null)
                List<Role> roles = roleRepository.findByTypeRoleAndSchoolIsNull(RoleType.SCHOOL);
                return new ApiResponse<>(true, "Lấy danh sách role admin trường học thành công", mapRolesToResponses(roles));
            }

            // Logic: Admin trường chỉ xem được role của trường mình
            validateSchoolAccess(currentUser, schoolId);
            List<Role> roles = roleRepository.findBySchoolId(schoolId);
            return new ApiResponse<>(true, "Lấy danh sách role thành công", mapRolesToResponses(roles));
        }

        throw new AppException.BadRequestException("typeRole phải là PROVIDER hoặc SCHOOL");
    }

    @Override
    public ApiResponse<RoleResponse> getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role với id: " + id));

        User currentUser = getCurrentUser();

        // Kiểm tra xem user hiện tại có quyền xem role này không
        validateAccessToRole(currentUser, role);

        return new ApiResponse<>(true, "Lấy thông tin role thành công", toRoleResponse(role));
    }

    @Override
    public ApiResponse<RoleResponse> getRoleByName(String roleName, RoleType typeRole, Long schoolId) {
        User currentUser = getCurrentUser();

        // 1. Kiểm tra quyền của User trước khi query DB
        if (typeRole == RoleType.PROVIDER) {
            validateProviderAccess(currentUser);
        } else if (typeRole == RoleType.SCHOOL) {
            // Nếu là Provider xem role School -> Cho phép (bỏ qua check schoolId của user)
            if (currentUser.getRole().getTypeRole() != RoleType.PROVIDER) {
                validateSchoolAccess(currentUser, schoolId);
            }
        } else {
            throw new AppException.BadRequestException("typeRole không hợp lệ");
        }

        // 2. Tìm kiếm Role
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role: " + roleName));

        // 3. Validate logic khớp data
        if (role.getTypeRole() != typeRole) {
            throw new AppException.BadRequestException("Role tìm thấy không khớp loại role yêu cầu");
        }

        // Validate quyền truy cập vào role cụ thể tìm được
        validateAccessToRole(currentUser, role);

        return new ApiResponse<>(true, "Thành công", toRoleResponse(role));
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> createRole(RoleResponse request, Long createBy) {
        // Lưu ý: createBy ở controller truyền vào giờ chính là User ID lấy từ Context
        User currentUser = userRepository.findById(createBy)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("User không tồn tại"));

        // Validate quyền tạo
        if (request.getTypeRole() == RoleType.PROVIDER) {
            if (currentUser.getRole().getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Chỉ Provider mới được tạo role hệ thống");
            }
        } else {
            validateSchoolAccess(currentUser, request.getSchoolId());
        }

        // Validate trùng tên
        validateUnique(null, request.getRoleName(), request.getTypeRole(), request.getSchoolId());

        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy trường học"));
        }

        Role role = roleMapper.mapToEntityFromResponse(request, school);
        role.setCreateBy(createBy);
        role.setUpdateBy(createBy);

        Role savedRole = roleRepository.save(role);
        return new ApiResponse<>(true, "Tạo thành công", toRoleResponse(savedRole));
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> updateRole(Long id, RoleResponse request, Long updateBy) {
        Role target = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role ID: " + id));

        User currentUser = userRepository.findById(updateBy)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("User không tồn tại"));

        // Validate quyền sửa (User có được sửa role này không?)
        validateAccessToRole(currentUser, target);

        // Validate trùng tên (trừ chính nó)
        if (request.getRoleName() != null) {
            validateUnique(id, request.getRoleName(), target.getTypeRole(), request.getSchoolId());
        }

        School school = target.getSchool(); // Mặc định giữ school cũ nếu không truyền
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy trường học"));
        }

        roleMapper.updateEntityFromResponse(target, request, school);
        target.setUpdateBy(updateBy);

        Role updatedRole = roleRepository.save(target);
        return new ApiResponse<>(true, "Cập nhật thành công", toRoleResponse(updatedRole));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteRole(Long id) {
        Role target = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role ID: " + id));

        User currentUser = getCurrentUser();

        // Validate quyền xóa
        validateAccessToRole(currentUser, target);

        // Check ràng buộc dữ liệu
        Long count = userRepository.countByRoleId(id);
        if (count > 0) {
            throw new AppException.ConflictException("Không thể xóa role đang có " + count + " người dùng sử dụng.");
        }

        roleRepository.delete(target);
        return new ApiResponse<>(true, "Xóa thành công", "ID: " + id);
    }

    // ================= PRIVATE HELPER METHODS =================

    /**
     * Lấy User từ Security Context (Chuẩn Spring Security)
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException.AuthenticationException("Người dùng chưa đăng nhập");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException.ResourceNotFoundException(
                        "Không tìm thấy thông tin người dùng đang đăng nhập"));
    }

    /**
     * Helper kiểm tra xem User có quyền thao tác trên một Role cụ thể không
     */
    private void validateAccessToRole(User user, Role role) {
        if (role.getTypeRole() == RoleType.PROVIDER) {
            validateProviderAccess(user);
        } else {
            // Role trường học
            if (user.getRole().getTypeRole() == RoleType.PROVIDER) {
                // Provider có quyền xem/sửa role trường học không?
                // Tùy nghiệp vụ, ở code cũ bạn chặn Provider sửa role School cụ thể,
                // nhưng ở đây tôi giả định Provider có quyền tối cao hoặc code cũ bạn chặn.
                // Nếu code cũ chặn Provider can thiệp role School của trường cụ thể:
                if (role.getSchool() != null) {
                    // Provider không thuộc trường này -> Chặn (theo logic code cũ)
                    // Nếu muốn Provider được quyền hết thì bỏ dòng throw này.
                    throw new AppException.ForbiddenException("Provider không được can thiệp dữ liệu riêng của trường");
                }
            } else {
                // User là School Admin
                if (role.getSchool() == null) {
                    throw new AppException.ForbiddenException("Bạn không có quyền với role mẫu hệ thống");
                }
                validateSchoolAccess(user, role.getSchool().getId());
            }
        }
    }

    private void validateProviderAccess(User user) {
        if (user.getRole().getTypeRole() != RoleType.PROVIDER) {
            throw new AppException.ForbiddenException("Bạn không có quyền hệ thống (Provider)");
        }
    }

    private void validateSchoolAccess(User user, Long schoolId) {
        if (user.getRole().getTypeRole() != RoleType.SCHOOL) {
            throw new AppException.ForbiddenException("Bạn không phải Admin trường học");
        }
        if (schoolId == null) {
            throw new AppException.BadRequestException("schoolId là bắt buộc");
        }
        if (user.getSchool() == null || !schoolId.equals(user.getSchool().getId())) {
            throw new AppException.ForbiddenException("Bạn không thuộc trường học này");
        }
    }

    private RoleResponse toRoleResponse(Role role) {
        Long userCount = userRepository.countByRoleId(role.getId());
        return roleMapper.mapToResponse(role, userCount);
    }

    private List<RoleResponse> mapRolesToResponses(List<Role> roles) {
        if (roles == null || roles.isEmpty()) return List.of();

        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());
        Map<Long, Long> userCountsMap = userRepository.getUserCountsByRoleIds(roleIds);

        return roles.stream()
                .map(role -> {
                    Long userCount = userCountsMap.getOrDefault(role.getId(), 0L);
                    return roleMapper.mapToResponse(role, userCount);
                })
                .collect(Collectors.toList());
    }

    private void validateUnique(Long currentId, String roleName, RoleType typeRole, Long schoolId) {
        Map<String, String> errors = new HashMap<>();
        Long actualSchoolId = (typeRole == RoleType.PROVIDER) ? null : schoolId;

        if (typeRole == RoleType.SCHOOL && actualSchoolId == null) {
            errors.put("schoolId", "schoolId là bắt buộc khi typeRole là SCHOOL");
        }

        if (roleName != null && typeRole != null) {
            boolean exists;
            if (currentId == null) {
                exists = roleRepository.existsByRoleNameAndTypeRoleAndSchoolId(roleName, typeRole, actualSchoolId);
            } else {
                exists = roleRepository.existsByRoleNameAndTypeRoleAndSchoolIdAndIdNot(roleName, typeRole, actualSchoolId, currentId);
            }

            if (exists) {
                String msg = (typeRole == RoleType.PROVIDER)
                        ? "Tên role đã tồn tại trong hệ thống"
                        : "Tên role đã tồn tại trong trường này";
                errors.put("roleName", msg);
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
