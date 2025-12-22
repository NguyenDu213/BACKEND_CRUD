package com.backend.backend_crud.service.impl;

import com.backend.backend_crud.dto.request.SchoolRequest;
import com.backend.backend_crud.dto.request.UpdateSchoolRequest;
import com.backend.backend_crud.dto.response.SchoolResponse;
import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.School;
import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.entity.Gender;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.UserScope;
import com.backend.backend_crud.exception.AppException;
import com.backend.backend_crud.exception.ValidationException;
import com.backend.backend_crud.mapper.SchoolMapper;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import com.backend.backend_crud.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final SchoolMapper schoolMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<SchoolResponse> getAll() {
        return schoolRepository.findAll().stream()
                .map(schoolMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public SchoolResponse getById(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy trường học"));
        return schoolMapper.mapToResponse(school);
    }

    @Override
    @Transactional
    public SchoolResponse create(SchoolRequest request) {
        // 1. Check trùng
        validateUnique(null, request.getCode(), request.getName(), request.getEmail(), request.getHotline());

        User currentUser = getCurrentUser();

        School school = schoolMapper.mapToEntity(request);
        school.setCreateBy(currentUser.getId());
        school.setUpdateBy(currentUser.getId());

        // 2. Lưu School
        School savedSchool = schoolRepository.save(school);

        // 3. Tạo Role Admin duy nhất cho trường
        Role adminRole = createSchoolAdminRole(savedSchool);

        // 4. Tạo User Hiệu trưởng
        createPrincipalAccount(savedSchool, adminRole);

        return schoolMapper.mapToResponse(savedSchool);
    }

    @Override
    @Transactional
    public SchoolResponse update(Long id, UpdateSchoolRequest request) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy trường học"));

        // Check trùng
        validateUnique(id, request.getCode(), request.getName(), request.getEmail(), request.getHotline());

        // Map dữ liệu mới và Lưu
        schoolMapper.updateEntityFromRequest(school, request);

        User currentUser = getCurrentUser();
        school.setUpdateBy(currentUser.getId());

        return schoolMapper.mapToResponse(schoolRepository.save(school));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!schoolRepository.existsById(id)) {
            throw new AppException(404, "Không tìm thấy trường học để xóa");
        }
        schoolRepository.deleteById(id);
    }

    @Override
    public List<SchoolResponse> searchSchoolsByName(String name) {
        List<School> schools = schoolRepository.findByNameContainingIgnoreCase(name);

        return schools.stream()
                .map(schoolMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(401, "Người dùng chưa đăng nhập");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(404, "Không tìm thấy thông tin người dùng đang đăng nhập"));
    }

    // Check trùng
    private void validateUnique(Long currentId, String code, String name, String email, String hotline) {
        Map<String, String> errors = new HashMap<>();

        if (code != null) {
            boolean exists = (currentId == null)
                    ? schoolRepository.existsByCode(code)
                    : schoolRepository.existsByCodeAndIdNot(code, currentId);
            if (exists) errors.put("code", "Mã trường đã tồn tại");
        }

        if (name != null) {
            boolean exists = (currentId == null)
                    ? schoolRepository.existsByName(name)
                    : schoolRepository.existsByNameAndIdNot(name, currentId);
            if (exists) errors.put("name", "Tên trường đã tồn tại");
        }

        if (email != null) {
            boolean exists = (currentId == null)
                    ? schoolRepository.existsByEmail(email)
                    : schoolRepository.existsByEmailAndIdNot(email, currentId);
            if (exists) errors.put("email", "Email trường đã tồn tại");
        }

        if (hotline != null) {
            boolean exists = (currentId == null)
                    ? schoolRepository.existsByHotline(hotline)
                    : schoolRepository.existsByHotlineAndIdNot(hotline, currentId);
            if (exists) errors.put("hotline", "Hotline đã tồn tại");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    // Tạo Role Admin
    private Role createSchoolAdminRole(School school) {
        Role adminRole = Role.builder()
                .roleName("SCHOOL_ADMIN")
                .description("Quản trị viên trường học")
                .typeRole(RoleType.SCHOOL)
                .school(school)
                .build();
        return roleRepository.save(adminRole);
    }

    // Tạo User Hiệu trưởng
    private void createPrincipalAccount(School school, Role adminRole) {
        User principal = User.builder()
                .fullName(school.getPrincipalName())
                .email(school.getEmail())
                .password(passwordEncoder.encode("12345678"))
                .phoneNumber(school.getHotline())
                .address(school.getAddress())
                .birthYear(LocalDateTime.of(1980, 1, 1, 0, 0))
                .gender(Gender.MALE)
                .isActive(true)
                .scope(UserScope.SCHOOL)
                .school(school)
                .role(adminRole)
                .build();
        userRepository.save(principal);
    }
}