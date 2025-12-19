package com.backend.backend_crud.service.implement;

import com.backend.backend_crud.config.SecurityConfig;
import com.backend.backend_crud.dto.request.UserRequest;
import com.backend.backend_crud.dto.response.ApiResponse;
import com.backend.backend_crud.dto.response.UserResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.entity.UserScope;
import com.backend.backend_crud.mapper.UserMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.service.UserService;
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
    public ApiResponse<List<UserResponse>> getAll() {
        try {
            List<User> listUser = userRepository.findAll();
            List<UserResponse> response = listUser.stream().map(UserMapper::mapToResponse).toList();

            return new ApiResponse<>(true, "Lấy danh sách User thành công", response);
        }
        catch (Exception ex){
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<UserResponse> createUser(UserRequest request) {
        try {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email đã tồn tại");
            }
            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
            School school = null;
            if (request.getSchoolId() != null) {
                school = schoolRepository.findById(request.getSchoolId())
                        .orElseThrow(() -> new RuntimeException("School không tồn tại"));
            }

            // 4. Validate scope & school
            if (request.getScope() == UserScope.SCHOOL && school == null) {
                throw new RuntimeException("User scope SCHOOL bắt buộc phải có schoolId");
            }

            if (request.getScope() == UserScope.PROVIDER && school != null) {
                throw new RuntimeException("User scope SYSTEM không được gán school");
            }
            User user = UserMapper.mapToEntity(request, school, role);
            user.setPassword(SecurityConfig.passwordEncoder().encode(request.getPassword()));
            user.setCreateBy(1L);
            user.setUpdateBy(1L);
            user.setCreatedAt(LocalDateTime.now());
            userRepository.save(user);

            UserResponse response = UserMapper.mapToResponse(user);

            return new ApiResponse<>(true, "Tạo mới User thành công", response);
        }
        catch (Exception ex){
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<UserResponse> updateUser(
            UserRequest request,
            Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            Role role = roleRepository.findById(request.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

            School school = null;
            if (request.getSchoolId() != null) {
                school = schoolRepository.findById(request.getSchoolId())
                        .orElseThrow(() -> new RuntimeException("School không tồn tại"));
            }

            user.setFullName(request.getFullName());
            user.setGender(request.getGender());
            user.setBirthYear(request.getBirthYear());
            user.setAddress(request.getAddress());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setEmail(request.getEmail());
            user.setScope(request.getScope());
            user.setIsActive(request.getIsActive());
            user.setRole(role);
            user.setSchool(school);
            user.setPassword(SecurityConfig.passwordEncoder().encode(request.getPassword()));
            user.setUpdateBy(1L);
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
}
