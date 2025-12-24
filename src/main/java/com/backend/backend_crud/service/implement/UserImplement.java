package com.backend.backend_crud.service.implement;

import com.backend.backend_crud.config.SecurityConfig;
import com.backend.backend_crud.dto.request.UpdateUserRequest;
import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.UserResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.entity.UserScope;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.mapper.UserMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserImplement implements UserService {
    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final RoleRepository roleRepository;

    @Override
    public ApiResponse<List<UserResponse>> getAll(Long userId) {
        try {
            UserScope scope = userRepository.findScopeByUserId(userId);
            String role = userRepository.findRoleNameByUserId(userId);
            if (scope == UserScope.PROVIDER) {
                if (role.equals("SYSTEM_ADMIN")) {
                    List<User> listUser = userRepository.findAllSystemUsers();
                    List<UserResponse> response = listUser.stream().map(UserMapper::mapToResponse).toList();
                    return new ApiResponse<>(true, "Lấy danh sách User thành công", response);
                }
            } else {
                if (role.equals("SCHOOL_ADMIN")) {
                    Long idSchool = userRepository.findSchoolIdByUserId(userId);
                    if (idSchool == null) {
                        return new ApiResponse<>(
                                false,
                                "Tài khoản không thuộc trường nào và không phải tài khoản hệ thống",
                                null);
                    }
                    List<User> listUser = userRepository.findAllSchoolUsers(idSchool);
                    List<UserResponse> response = listUser.stream().map(UserMapper::mapToResponse).toList();
                    return new ApiResponse<>(true, "Lấy danh sách User thành công", response);
                }
            }
            return new ApiResponse<>(
                    false,
                    "Không lấy được danh sách người dùng",
                    null);
        } catch (Exception ex) {
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<UserResponse> createUser(UserRequest request, Long userId) {
        UserScope scope = userRepository.findScopeByUserId(userId);
        String roleName = userRepository.findRoleNameByUserId(userId);
        if (scope == UserScope.PROVIDER) {
            if (roleName.equals("SYSTEM_ADMIN")) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new AppException.BadRequestException("Email đã tồn tại");
                }
                Role role = roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new AppException.ResourceNotFoundException("Role không tồn tại"));
                if (!request.getScope().equals(scope)) {
                    return new ApiResponse<>(false, "Không được chọn khác scope người dùng hiện tại", null);
                }
                RoleType creatorType = roleRepository.findTypeRoleByRoleId(userId);
                RoleType targetType = roleRepository.findTypeRoleByRoleId(request.getRoleId());

                if (!creatorType.equals(targetType)) {
                    throw new AppException.ForbiddenException(
                            "Không được tạo tài khoản với type role khác người dùng hiện tại");
                }

                School school = null;
                User user = UserMapper.mapToEntity(request, school, role);
                user.setSchool(null);
                user.setPassword(SecurityConfig.passwordEncoder().encode(request.getPassword()));
                user.setCreateBy(userId);
                user.setUpdateBy(userId);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);

                UserResponse response = UserMapper.mapToResponse(user);

                return new ApiResponse<>(true, "Tạo mới User thành công", response);
            }
        } else {
            if (roleName.equals("SCHOOL_ADMIN")) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new AppException.BadRequestException("Email đã tồn tại");
                }
                Role role = roleRepository.findById(request.getRoleId())
                        .orElseThrow(() -> new AppException.ResourceNotFoundException("Role không tồn tại"));
                if (!request.getScope().equals(scope)) {
                    return new ApiResponse<>(false, "Không được chọn khác scope người dùng hiện tại", null);
                }
                School school = null;
                if (request.getSchoolId() != null) {
                    school = schoolRepository.findById(request.getSchoolId())
                            .orElseThrow(() -> new AppException.ResourceNotFoundException("School không tồn tại"));
                }
                Long idSchool = userRepository.findSchoolIdByUserId(userId);
                if (request.getSchoolId() == null || !request.getSchoolId().equals(idSchool)) {
                    return new ApiResponse<>(
                            false,
                            "Tài khoản không thuộc trường này",
                            null);
                }
                RoleType creatorType = roleRepository.findTypeRoleByRoleId(userId);
                RoleType targetType = roleRepository.findTypeRoleByRoleId(request.getRoleId());

                if (!creatorType.equals(targetType)) {
                    throw new AppException.ForbiddenException(
                            "Không được tạo tài khoản với type role khác người dùng hiện tại");
                }

                User user = UserMapper.mapToEntity(request, school, role);
                user.setPassword(SecurityConfig.passwordEncoder().encode(request.getPassword()));
                user.setCreateBy(userId);
                user.setUpdateBy(userId);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);

                UserResponse response = UserMapper.mapToResponse(user);

                return new ApiResponse<>(true, "Tạo mới User thành công", response);
            }
        }
        return new ApiResponse<>(
                false,
                "Không tạo được người dùng",
                null);
    }

    @Override
    public ApiResponse<UserResponse> updateUser(
            UpdateUserRequest request,
            Long userId,
            Long updateBy) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("User không tồn tại"));
        boolean emailExists = userRepository.existsByEmailAndIdNot(
                request.getEmail(),
                userId);
        if (emailExists) {
            return new ApiResponse<>(
                    false,
                    "Email đã tồn tại",
                    null);
        }
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Role không tồn tại"));

        School school = null;
        if (request.getSchoolId() != null) {
            school = schoolRepository.findById(request.getSchoolId())
                    .orElseThrow(() -> new AppException.ResourceNotFoundException("School không tồn tại"));
        }
        UserScope scope = userRepository.findScopeByUserId(updateBy);
        String roleName = userRepository.findRoleNameByUserId(updateBy);
        if (scope == UserScope.PROVIDER) {
            if (roleName.equals("SYSTEM_ADMIN")) {

                if (!request.getScope().equals(scope)) {
                    return new ApiResponse<>(
                            false,
                            "Không được chọn khác scope của người dùng hiện tại",
                            null);
                }

                RoleType creatorType = roleRepository.findTypeRoleByRoleId(updateBy);
                RoleType targetType = roleRepository.findTypeRoleByRoleId(request.getRoleId());

                if (!creatorType.equals(targetType)) {
                    throw new AppException.ForbiddenException(
                            "Không được tạo tài khoản với type role khác người dùng hiện tại");
                }

                user.setSchool(null);
            }
        } else {
            if (roleName.equals("SCHOOL_ADMIN")) {

                if (!request.getScope().equals(scope)) {
                    return new ApiResponse<>(
                            false,
                            "Không được chọn khác scope người dùng hiện tại",
                            null);
                }

                RoleType updaterType = roleRepository.findTypeRoleByRoleId(updateBy);
                RoleType targetType = roleRepository.findTypeRoleByRoleId(request.getRoleId());

                if (!updaterType.equals(targetType)) {
                    throw new AppException.ForbiddenException(
                            "Không được tạo tài khoản với type role khác người dùng hiện tại");
                }

                Long idSchool = userRepository.findSchoolIdByUserId(updateBy);
                if (!request.getSchoolId().equals(idSchool)) {
                    return new ApiResponse<>(
                            false,
                            "Tài khoản không thuộc trường này",
                            null);
                }

                user.setSchool(school);
            }
        }
        String emailToUpdate;
        if (request.getEmail().isBlank()) {
            emailToUpdate = user.getEmail();
        } else {
            emailToUpdate = request.getEmail();
        }

        String passwordUpdate;
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            passwordUpdate = user.getPassword();
        } else {
            passwordUpdate = request.getPassword();
        }

        user.setFullName(request.getFullName());
        user.setGender(request.getGender());
        user.setBirthYear(request.getBirthYear());
        user.setAddress(request.getAddress());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setEmail(emailToUpdate);
        user.setScope(request.getScope());
        user.setIsActive(request.getIsActive());
        user.setRole(role);
        user.setPassword(SecurityConfig.passwordEncoder().encode(passwordUpdate));
        user.setUpdateBy(updateBy);
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        UserResponse response = UserMapper.mapToResponse(user);
        return new ApiResponse<>(true, "Cập nhật User thành công", response);
    }

    @Override
    public ApiResponse<UserResponse> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("User không tồn tại"));

        userRepository.delete(user);

        return new ApiResponse<>(true, "Xóa User thành công", null);
    }

    @Override
    public ApiResponse<List<UserResponse>> searchUser(String search, Long userId) {

        UserScope scope = userRepository.findScopeByUserId(userId);
        String role = userRepository.findRoleNameByUserId(userId);
        if (scope == UserScope.PROVIDER) {
            if (role.equals("SYSTEM_ADMIN")) {
                if (search != null) {
                    search = search.trim();
                }
                List<User> listUser = userRepository.searchByName(search, UserScope.PROVIDER);
                List<UserResponse> response = listUser.stream().map(UserMapper::mapToResponse).toList();
                return new ApiResponse<>(true, "Lấy danh sách User thành công", response);
            }
        } else {
            if (role.equals("SCHOOL_ADMIN")) {
                Long idSchool = userRepository.findSchoolIdByUserId(userId);
                if (idSchool == null) {
                    return new ApiResponse<>(
                            false,
                            "Tài khoản không thuộc trường nào và không phải tài khoản hệ thống",
                            null);
                }
                if (search != null) {
                    search = search.trim();
                }

                List<User> listUser = userRepository.searchBySchoolAndName(idSchool, UserScope.SCHOOL, search);
                List<UserResponse> response = listUser.stream().map(UserMapper::mapToResponse).toList();
                return new ApiResponse<>(true, "Lấy danh sách User thành công", response);
            }
        }
        return new ApiResponse<>(
                false,
                "Không lấy được danh sách người dùng",
                null);
    }

}
