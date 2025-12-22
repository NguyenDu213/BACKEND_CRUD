package com.backend.backend_crud.service;

import com.backend.backend_crud.dto.request.RoleRequest;
import com.backend.backend_crud.dto.request.UpdateRoleRequest;
import com.backend.backend_crud.dto.response.RoleResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.exception.ResourceNotFoundException;
import com.backend.backend_crud.mapper.RoleMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xử lý business logic cho Role
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final RoleMapper roleMapper;

    /**
     * Lấy danh sách tất cả roles với filter tùy chọn
     *
     * @param typeRole Loại role (PROVIDER hoặc SCHOOL)
     * @param schoolId ID của school (null nếu là system role)
     * @return Danh sách RoleResponse
     */
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles(RoleType typeRole, Long schoolId) {
        List<Role> roles;

        if (typeRole != null && schoolId != null) {
            // Filter theo cả typeRole và schoolId (bao gồm system roles)
            roles = roleRepository.findByTypeRoleAndSchoolIdOrSystem(typeRole, schoolId);
        } else if (typeRole != null) {
            // Filter theo typeRole
            roles = roleRepository.findByTypeRole(typeRole);
        } else if (schoolId != null) {
            // Filter theo schoolId (bao gồm cả system roles với schoolId = null)
            roles = roleRepository.findBySchoolIdOrSystemRoles(schoolId);
        } else {
            // Lấy tất cả roles
            roles = roleRepository.findAll();
        }

        return roles.stream()
                .map(role -> {
                    Long userCount = userRepository.countByRoleId(role.getId());
                    return roleMapper.mapToResponse(role, userCount);
                })
                .collect(Collectors.toList());
    }

    /**
     * Lấy role theo ID
     *
     * @param id ID của role
     * @return RoleResponse
     * @throws ResourceNotFoundException nếu không tìm thấy role
     */
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với ID: " + id));

        Long userCount = userRepository.countByRoleId(role.getId());
        return roleMapper.mapToResponse(role, userCount);
    }

    /**
     * Tạo role mới
     *
     * @param request RoleRequest chứa thông tin role
     * @param createBy ID của user tạo (từ security context hoặc request)
     * @return RoleResponse
     */
    @Transactional
    public RoleResponse createRole(RoleRequest request, Long createBy) {
        // Lấy School nếu có và validate roleName không trùng
        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy school với ID: " + request.getSchoolId()));
            roleRepository.findByRoleNameAndSchool(request.getRoleName(), school)
                    .ifPresent(role -> {
                        throw new RuntimeException("Role với tên '" + request.getRoleName() + "' đã tồn tại trong school này");
                    });
        } else {
            roleRepository.findByRoleName(request.getRoleName())
                    .ifPresent(role -> {
                        throw new RuntimeException("Role với tên '" + request.getRoleName() + "' đã tồn tại");
                    });
        }

        // Tạo role mới
        Role role = roleMapper.mapToEntity(request, school);
        role.setCreateBy(createBy);
        role.setUpdateBy(createBy);

        role = roleRepository.save(role);
        log.info("Created role with ID: {}", role.getId());

        Long userCount = userRepository.countByRoleId(role.getId());
        return roleMapper.mapToResponse(role, userCount);
    }

    /**
     * Cập nhật role
     *
     * @param id      ID của role cần cập nhật
     * @param request UpdateRoleRequest chứa thông tin cập nhật
     * @param updateBy ID của user cập nhật (từ security context hoặc request)
     * @return RoleResponse
     * @throws ResourceNotFoundException nếu không tìm thấy role
     */
    @Transactional
    public RoleResponse updateRole(Long id, UpdateRoleRequest request, Long updateBy) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với ID: " + id));

        // Validate roleName không trùng (nếu có thay đổi)
        if (request.getRoleName() != null && !request.getRoleName().equals(role.getRoleName())) {
            School schoolToCheck = null;
            if (request.getSchoolId() != null) {
                schoolToCheck = schoolRepository.findById(request.getSchoolId())
                        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy school với ID: " + request.getSchoolId()));
            } else if (role.getSchool() != null) {
                schoolToCheck = role.getSchool();
            }
            
            if (schoolToCheck != null) {
                roleRepository.findByRoleNameAndSchool(request.getRoleName(), schoolToCheck)
                        .ifPresent(existingRole -> {
                            if (!existingRole.getId().equals(id)) {
                                throw new RuntimeException("Role với tên '" + request.getRoleName() + "' đã tồn tại trong school này");
                            }
                        });
            } else {
                roleRepository.findByRoleName(request.getRoleName())
                        .ifPresent(existingRole -> {
                            if (!existingRole.getId().equals(id)) {
                                throw new RuntimeException("Role với tên '" + request.getRoleName() + "' đã tồn tại");
                            }
                        });
            }
        }

        // Cập nhật các trường
        if (request.getRoleName() != null) {
            role.setRoleName(request.getRoleName());
        }
        if (request.getTypeRole() != null) {
            role.setTypeRole(request.getTypeRole());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }
        if (request.getSchoolId() != null) {
            School school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy school với ID: " + request.getSchoolId()));
            role.setSchool(school);
        } else if (request.getSchoolId() == null && request.getTypeRole() == RoleType.PROVIDER) {
            // Nếu typeRole là PROVIDER, schoolId phải là null
            role.setSchool(null);
        }

        role.setUpdateBy(updateBy);
        role = roleRepository.save(role);
        log.info("Updated role with ID: {}", role.getId());

        Long userCount = userRepository.countByRoleId(role.getId());
        return roleMapper.mapToResponse(role, userCount);
    }

    /**
     * Xóa role
     *
     * @param id ID của role cần xóa
     * @throws ResourceNotFoundException nếu không tìm thấy role
     * @throws RuntimeException nếu role đang được sử dụng bởi users
     */
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy role với ID: " + id));

        // Kiểm tra xem role có đang được sử dụng không
        Long userCount = userRepository.countByRoleId(id);
        if (userCount > 0) {
            throw new RuntimeException("Không thể xóa role này vì đang có " + userCount + " người dùng sử dụng. Vui lòng gán role khác cho các người dùng trước khi xóa.");
        }

        roleRepository.delete(role);
        log.info("Deleted role with ID: {}", id);
    }
}
