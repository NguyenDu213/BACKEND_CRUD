package com.backend.backend_crud.service.implement;

import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.mapper.RoleMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.JwtService;
import com.backend.backend_crud.service.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;
    private final JwtService jwtService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ApiResponse<List<RoleResponse>> getAllRoles(RoleType typeRole, Long schoolId) {
        User currentUser = getCurrentUser();

        if (typeRole == RoleType.PROVIDER) {
            // Check quyền Admin-Provider
            if (currentUser.getRole().getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Bạn không có quyền xem role hệ thống");
            }
            // Trả về tất cả role hệ thống (schoolId is null)
            List<RoleResponse> responses = roleRepository.findBySchoolIsNull().stream()
                    .map(this::toRoleResponse).collect(Collectors.toList());
            return new ApiResponse<>(true, "Lấy danh sách thành công", responses);
        } else if (typeRole == RoleType.SCHOOL) {
            // Check quyền Admin-School
            if (currentUser.getRole().getTypeRole() != RoleType.SCHOOL) {
                throw new AppException.ForbiddenException("Bạn không có quyền xem role của trường học");
            }
            // Validate schoolId
            if (schoolId == null) {
                throw new AppException.BadRequestException("schoolId là bắt buộc khi typeRole là SCHOOL");
            }
            if (currentUser.getSchool() == null || !schoolId.equals(currentUser.getSchool().getId())) {
                throw new AppException.ForbiddenException("Bạn không thuộc trường này");
            }
            // Lấy role theo schoolId
            List<RoleResponse> responses = roleRepository.findBySchoolId(schoolId).stream()
                    .map(this::toRoleResponse).collect(Collectors.toList());
            return new ApiResponse<>(true, "Lấy danh sách thành công", responses);
        } else {
            // typeRole là null hoặc không hợp lệ
            throw new AppException.BadRequestException("typeRole phải là PROVIDER hoặc SCHOOL");
        }
    }

    @Override
    public ApiResponse<RoleResponse> getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role với id: " + id));
        return new ApiResponse<>(true, "Lấy thông tin role thành công", toRoleResponse(role));
    }

    @Override
    public ApiResponse<RoleResponse> getRoleByName(String roleName, Long schoolId) {
        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(
                        () -> new AppException.ResourceNotFoundException("Không tìm thấy role với tên: " + roleName));
        return new ApiResponse<>(true, "Thành công", toRoleResponse(role));
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> createRole(RoleResponse request, Long createBy) {
        User user = userRepository.findById(createBy)
                .orElseThrow(
                        () -> new AppException.ResourceNotFoundException("Không tìm thấy user với id: " + createBy));

        // Kiểm tra quyền
        if (request.getTypeRole() == RoleType.PROVIDER) {
            if (user.getRole().getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Chỉ user PROVIDER mới có quyền tạo role hệ thống");
            }
        } else {
            if (user.getRole().getTypeRole() != RoleType.SCHOOL) {
                throw new AppException.ForbiddenException("Chỉ user SCHOOL mới có quyền tạo role cho trường học");
            }
            if (request.getSchoolId() == null) {
                throw new AppException.BadRequestException("schoolId là bắt buộc khi typeRole là SCHOOL");
            }
            if (user.getSchool() == null || !request.getSchoolId().equals(user.getSchool().getId())) {
                throw new AppException.ForbiddenException("Bạn không có quyền tạo role cho trường khác");
            }
        }

        // Kiểm tra tên role đã tồn tại
        if (roleRepository.existsByRoleName(request.getRoleName())) {
            throw new AppException.ConflictException(
                    "Tên role '" + request.getRoleName() + "' đã tồn tại trong hệ thống");
        }

        // Lấy school nếu có
        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new AppException.ResourceNotFoundException(
                            "Không tìm thấy school với id: " + request.getSchoolId()));
        }

        Role role = mapToEntityFromResponse(request, school);
        return new ApiResponse<>(true, "Tạo thành công", toRoleResponse(roleRepository.save(role)));
    }

    @Override
    @Transactional
    public ApiResponse<RoleResponse> updateRole(Long id, RoleResponse request, Long updateBy) {
        Role target = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy ID: " + id));
        User user = userRepository.findById(updateBy)
                .orElseThrow(
                        () -> new AppException.ResourceNotFoundException("Không tìm thấy user với id: " + updateBy));

        // Kiểm tra quyền
        if (target.getTypeRole() == RoleType.PROVIDER) {
            if (user.getRole().getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Chỉ user PROVIDER mới có quyền cập nhật role hệ thống");
            }
        } else {
            if (user.getRole().getTypeRole() != RoleType.SCHOOL) {
                throw new AppException.ForbiddenException("Chỉ user SCHOOL mới có quyền cập nhật role của trường học");
            }
            if (target.getSchool() == null || user.getSchool() == null
                    || !target.getSchool().getId().equals(user.getSchool().getId())) {
                throw new AppException.ForbiddenException("Bạn không có quyền cập nhật role của trường khác");
            }
        }

        // Kiểm tra tên role đã tồn tại (trừ chính nó)
        if (request.getRoleName() != null && roleRepository.existsByRoleNameAndIdNot(request.getRoleName(), id)) {
            throw new AppException.ConflictException(
                    "Tên role '" + request.getRoleName() + "' đã tồn tại trong hệ thống");
        }

        // Lấy school nếu có
        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new AppException.ResourceNotFoundException(
                            "Không tìm thấy school với id: " + request.getSchoolId()));
        }

        // Cập nhật entity từ response
        updateEntityFromResponse(target, request, school);
        return new ApiResponse<>(true, "Cập nhật thành công", toRoleResponse(roleRepository.save(target)));
    }

    @Override
    @Transactional
    public ApiResponse<String> deleteRole(Long id) {
        Role target = roleRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy ID: " + id));
        User currentUser = getCurrentUser();

        // Check quyền xóa
        if (target.getTypeRole() == RoleType.PROVIDER) {
            if (currentUser.getRole().getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Bạn không có quyền xóa role hệ thống");
            }
        } else {
            if (currentUser.getRole().getTypeRole() != RoleType.SCHOOL
                    || target.getSchool() == null
                    || currentUser.getSchool() == null
                    || !target.getSchool().getId().equals(currentUser.getSchool().getId())) {
                throw new AppException.ForbiddenException("Bạn không có quyền xóa role của trường khác");
            }
        }

        // Check user count
        Long count = userRepository.countByRoleId(id);
        if (count > 0) {
            throw new AppException.ConflictException("Không thể xóa role này vì đang có " + count
                    + " người dùng đang sử dụng. Vui lòng gán role khác cho các người dùng trước khi xóa.");
        }

        roleRepository.delete(target);
        return new ApiResponse<>(true, "Xóa thành công", "ID: " + id);
    }

    /**
     * Lấy current user từ JWT token trong request header
     */
    private User getCurrentUser() {
        String authHeader = httpServletRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException.TokenException("Token không hợp lệ");
        }

        String token = authHeader.substring(7);
        jwtService.validateAccessToken(token);

        Long userId = jwtService.getUserIdFromToken(token);
        return userRepository.findById(userId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy user với id: " + userId));
    }

    /**
     * Helper sử dụng mapToResponse của RoleMapper
     */
    private RoleResponse toRoleResponse(Role role) {
        return roleMapper.mapToResponse(role, userRepository.countByRoleId(role.getId()));
    }

    /**
     * Map từ RoleResponse -> Entity (dùng cho create)
     */
    private Role mapToEntityFromResponse(RoleResponse response, School school) {
        if (response == null)
            return null;
        return Role.builder()
                .roleName(response.getRoleName())
                .typeRole(response.getTypeRole())
                .description(response.getDescription())
                .school(school)
                .build();
    }

    /**
     * Cập nhật Entity từ RoleResponse (dùng cho update)
     */
    private void updateEntityFromResponse(Role role, RoleResponse response, School school) {
        if (response == null || role == null)
            return;
        if (response.getRoleName() != null) {
            role.setRoleName(response.getRoleName());
        }
        if (response.getTypeRole() != null) {
            role.setTypeRole(response.getTypeRole());
        }
        if (response.getDescription() != null) {
            role.setDescription(response.getDescription());
        }
        role.setSchool(school);
    }
}
