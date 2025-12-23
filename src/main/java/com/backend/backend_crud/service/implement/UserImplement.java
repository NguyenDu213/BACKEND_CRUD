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
import jakarta.transaction.Transactional;
import com.backend.backend_crud.mapper.UserMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Service
public class UserImplement implements UserService {
    private  final UserRepository userRepository;
    private  final SchoolRepository schoolRepository;
    private  final RoleRepository roleRepository;

    @Override
    public ApiResponse<List<UserResponse>> getAll(Long userId) {
        try {
            UserScope scope = userRepository.findScopeByUserId(userId);
            String role = userRepository.findRoleNameByUserId(userId);
            if (scope == UserScope.PROVIDER){
                if (role.equals("SYSTEM_ADMIN")){
                    List<User> listUser = userRepository.findAllSystemUsers();
                    List<UserResponse> response = listUser.stream().map(UserMapper::mapToResponse).toList();
                    return new ApiResponse<>(true, "Lấy danh sách User thành công", response);
                }
            }else {
                if (role.equals("SCHOOL_ADMIN")) {
                    Long idSchool = userRepository.findSchoolIdByUserId(userId);
                    if (idSchool == null ){
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
                    null
            );
        }
        catch (Exception ex){
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<UserResponse> createUser(UserRequest request, Long userId) {
        try {
            UserScope scope = userRepository.findScopeByUserId(userId);
            String roleName = userRepository.findRoleNameByUserId(userId);
            if (scope == UserScope.PROVIDER) {
                if (roleName.equals("SYSTEM_ADMIN")){
                    if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email đã tồn tại");
                    }
                    Role role = roleRepository.findById(request.getRoleId())
                            .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
                    if (!request.getScope().equals(scope)){
                        return new ApiResponse<>(false, "Không được chọn role khác scope người dùng hiện tại", null);
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
                if (roleName.equals("SCHOOL_ADMIN")){
                    if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email đã tồn tại");
                    }
                    Role role = roleRepository.findById(request.getRoleId())
                            .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
                    if (!request.getScope().equals(scope)){
                        return new ApiResponse<>(false, "Chọn được chọn role khác scope người dùng hiện tại", null);
                    }
                    School school = null;
                    if (request.getSchoolId() != null) {
                        school = schoolRepository.findById(request.getSchoolId())
                                .orElseThrow(() -> new RuntimeException("School không tồn tại"));
                    }
                    Long idSchool = userRepository.findSchoolIdByUserId(userId);
                    if (request.getSchoolId() == null || !request.getSchoolId().equals(idSchool)){
                        return new ApiResponse<>(
                                false,
                                "Tài khoản không thuộc trường này",
                                null);
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
                    null
            );
        }
        catch (Exception ex){
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<UserResponse> updateUser(
            UpdateUserRequest request,
            Long userId,
            Long updateBy) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));
            boolean emailExists =
                    userRepository.existsByEmailAndIdNot(
                            request.getEmail(),
                            userId
                    );
            if (emailExists) {
                return new ApiResponse<>(
                        false,
                        "Email đã tồn tại",
                        null
                );
            }
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

            School school = null;
            if (request.getSchoolId() != null) {
                school = schoolRepository.findById(request.getSchoolId())
                        .orElseThrow(() -> new RuntimeException("School không tồn tại"));
            }
            UserScope scope = userRepository.findScopeByUserId(updateBy);
            String roleName = userRepository.findRoleNameByUserId(updateBy);
            if (scope == UserScope.PROVIDER) {
                if (roleName.equals("SYSTEM_ADMIN")) {

                    if (!request.getScope().equals(scope)) {
                        return new ApiResponse<>(
                                false,
                                "Không được chọn role khác scope của người dùng hiện tại",
                                null
                        );
                    }

                    user.setSchool(null);
                }
            } else {
                if (roleName.equals("SCHOOL_ADMIN")) {

                    if (!request.getScope().equals(scope)) {
                        return new ApiResponse<>(
                                false,
                                "Không được chọn role khác scope người dùng hiện tại",
                                null
                        );
                    }

                    Long idSchool = userRepository.findSchoolIdByUserId(updateBy);
                    if (!request.getSchoolId().equals(idSchool)) {
                        return new ApiResponse<>(
                                false,
                                "Tài khoản không thuộc trường này",
                                null
                        );
                    }

                    user.setSchool(school);
                }
            }
            String emailToUpdate;
            if (request.getEmail().isBlank()){
                emailToUpdate = user.getEmail();
            } else{
                emailToUpdate = request.getEmail();
            }

            String passwordUpdate;
            if (!request.getEmail().isBlank()){
                passwordUpdate = user.getPassword();
            } else{
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
        catch (Exception ex){
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<UserResponse> deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            userRepository.delete(user);

            return new ApiResponse<>(true, "Xóa User thành công", null);
        }
        catch (Exception ex){
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<UserResponse>> searchUser(String search, Long userId) {
        try {
            UserScope scope = userRepository.findScopeByUserId(userId);
            String role = userRepository.findRoleNameByUserId(userId);
            if (scope == UserScope.PROVIDER){
                if (role.equals("SYSTEM_ADMIN")){
                    if (search != null) {
                        search = search.trim();
                    }
                    List<User> listUser = userRepository.searchByName(search, UserScope.PROVIDER);
                    List<UserResponse> response = listUser.stream().map(UserMapper::mapToResponse).toList();
                    return new ApiResponse<>(true, "Lấy danh sách User thành công", response);
                }
            }else {
                if (role.equals("SCHOOL_ADMIN")) {
                    Long idSchool = userRepository.findSchoolIdByUserId(userId);
                    if (idSchool == null ){
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
                    null
            );
        }
        catch (Exception ex){
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<List<UserResponse>> getUsersByRoleId(Long roleId, Long currentUserId) {
        // Kiểm tra role có tồn tại không
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role với id: " + roleId));
        
        // Kiểm tra quyền
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy user với id: " + currentUserId));
        
        if (role.getTypeRole() == RoleType.PROVIDER) {
            if (currentUser.getRole().getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Bạn không có quyền xem users của role hệ thống");
            }
        } else {
            if (currentUser.getRole().getTypeRole() != RoleType.SCHOOL) {
                throw new AppException.ForbiddenException("Bạn không có quyền xem users của role trường học");
            }
            if (role.getSchool() != null && currentUser.getSchool() != null
                    && !role.getSchool().getId().equals(currentUser.getSchool().getId())) {
                throw new AppException.ForbiddenException("Bạn không có quyền xem users của role trường khác");
            }
        }
        
        // Lấy danh sách users theo roleId
        List<User> users = userRepository.findByRoleId(roleId);
        List<UserResponse> response = users.stream().map(UserMapper::mapToResponse).toList();
        return new ApiResponse<>(true, "Lấy danh sách users thành công", response);
    }

    @Override
    public ApiResponse<Boolean> isRoleInUse(Long roleId) {
        try {
            Long count = userRepository.countByRoleId(roleId);
            return new ApiResponse<>(true, "Kiểm tra thành công", count > 0);
        } catch (Exception ex) {
            return new ApiResponse<>(false, ex.getMessage(), false);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> reassignRole(Long oldRoleId, Long newRoleId, Long currentUserId) {
        // Kiểm tra roles có tồn tại không
        Role oldRole = roleRepository.findById(oldRoleId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role cũ với id: " + oldRoleId));
        Role newRole = roleRepository.findById(newRoleId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy role mới với id: " + newRoleId));
        
        // Kiểm tra quyền
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException.ResourceNotFoundException("Không tìm thấy user với id: " + currentUserId));
        
        // Kiểm tra quyền với oldRole
        if (oldRole.getTypeRole() == RoleType.PROVIDER) {
            if (currentUser.getRole().getTypeRole() != RoleType.PROVIDER) {
                throw new AppException.ForbiddenException("Bạn không có quyền gán lại role hệ thống");
            }
        } else {
            if (currentUser.getRole().getTypeRole() != RoleType.SCHOOL) {
                throw new AppException.ForbiddenException("Bạn không có quyền gán lại role trường học");
            }
            if (oldRole.getSchool() != null && currentUser.getSchool() != null
                    && !oldRole.getSchool().getId().equals(currentUser.getSchool().getId())) {
                throw new AppException.ForbiddenException("Bạn không có quyền gán lại role của trường khác");
            }
        }
        
        // Kiểm tra newRole phải cùng typeRole với oldRole
        if (!oldRole.getTypeRole().equals(newRole.getTypeRole())) {
            throw new AppException.BadRequestException("Role mới phải cùng loại với role cũ");
        }
        
        // Kiểm tra nếu là SCHOOL role, phải cùng school
        if (oldRole.getTypeRole() == RoleType.SCHOOL) {
            if (oldRole.getSchool() != null && newRole.getSchool() != null
                    && !oldRole.getSchool().getId().equals(newRole.getSchool().getId())) {
                throw new AppException.BadRequestException("Role mới phải cùng trường với role cũ");
            }
        }
        
        // Lấy danh sách users đang dùng oldRole
        List<User> users = userRepository.findByRoleId(oldRoleId);
        if (users.isEmpty()) {
            return new ApiResponse<>(true, "Không có user nào đang sử dụng role này", "0");
        }
        
        // Gán role mới cho tất cả users
        int updatedCount = 0;
        for (User user : users) {
            user.setRole(newRole);
            user.setUpdateBy(currentUserId);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            updatedCount++;
        }
        
        return new ApiResponse<>(true, 
                "Đã gán role mới cho " + updatedCount + " người dùng", 
                String.valueOf(updatedCount));
    }
}
