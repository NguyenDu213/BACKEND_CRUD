package com.backend.backend_crud.config;

import com.backend.backend_crud.entity.*;
import com.backend.backend_crud.repository.RoleRepository;
import com.backend.backend_crud.repository.SchoolRepository;
import com.backend.backend_crud.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Long SYSTEM_USER_ID = 1L;

    @Override
    public void run(String... args) {
        seedSchools();
        seedRoles();
        seedUsers();
        log.info("✅ Hoàn thành seed dữ liệu mẫu!");
    }

    private void seedSchools() {
        if (schoolRepository.count() == 0) {
            List<School> schools = new ArrayList<>();

            School school1 = School.builder()
                    .name("Trường Tiểu học Nguyễn Du")
                    .code("TH001")
                    .email("thnguyendu@edu.vn")
                    .hotline("0241234567")
                    .address("123 Đường Nguyễn Du, Quận Hoàn Kiếm, Hà Nội")
                    .principalName("Nguyễn Văn An")
                    .build();
            school1.setCreateBy(SYSTEM_USER_ID);
            school1.setUpdateBy(SYSTEM_USER_ID);

            School school2 = School.builder()
                    .name("Trường THCS Lê Lợi")
                    .code("THCS002")
                    .email("thcsleloi@edu.vn")
                    .hotline("0242345678")
                    .address("456 Đường Lê Lợi, Quận Ba Đình, Hà Nội")
                    .principalName("Trần Thị Bình")
                    .build();
            school2.setCreateBy(SYSTEM_USER_ID);
            school2.setUpdateBy(SYSTEM_USER_ID);

            School school3 = School.builder()
                    .name("Trường THPT Chu Văn An")
                    .code("THPT003")
                    .email("thptchuvanan@edu.vn")
                    .hotline("0243456789")
                    .address("789 Đường Chu Văn An, Quận Đống Đa, Hà Nội")
                    .principalName("Lê Văn Cường")
                    .build();
            school3.setCreateBy(SYSTEM_USER_ID);
            school3.setUpdateBy(SYSTEM_USER_ID);

            schools.add(school1);
            schools.add(school2);
            schools.add(school3);

            schoolRepository.saveAll(schools);
            log.info("✅ Đã seed {} trường học", schools.size());
        } else {
            log.info("ℹ️ Trường học đã tồn tại, bỏ qua seed dữ liệu");
        }
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            List<Role> roles = new ArrayList<>();

            // Provider Roles
            Role systemAdmin = Role.builder()
                    .roleName("SYSTEM_ADMIN")
                    .typeRole(RoleType.PROVIDER)
                    .description("Quản trị viên hệ thống, có toàn quyền quản lý")
                    .school(null)
                    .build();
            systemAdmin.setCreateBy(SYSTEM_USER_ID);
            systemAdmin.setUpdateBy(SYSTEM_USER_ID);

            Role systemStaff = Role.builder()
                    .roleName("SYSTEM_STAFF")
                    .typeRole(RoleType.PROVIDER)
                    .description("Nhân viên hệ thống, quản lý các trường học")
                    .school(null)
                    .build();
            systemStaff.setCreateBy(SYSTEM_USER_ID);
            systemStaff.setUpdateBy(SYSTEM_USER_ID);

            roles.add(systemAdmin);
            roles.add(systemStaff);

            // School Roles - School 1
            School school1 = schoolRepository.findByCode("TH001").orElse(null);
            if (school1 != null) {
                Role schoolAdmin1 = Role.builder()
                        .roleName("SCHOOL_ADMIN")
                        .typeRole(RoleType.SCHOOL)
                        .description("Quản trị viên trường học")
                        .school(school1)
                        .build();
                schoolAdmin1.setCreateBy(SYSTEM_USER_ID);
                schoolAdmin1.setUpdateBy(SYSTEM_USER_ID);

                Role teacher1 = Role.builder()
                        .roleName("TEACHER")
                        .typeRole(RoleType.SCHOOL)
                        .description("Giáo viên trong trường học")
                        .school(school1)
                        .build();
                teacher1.setCreateBy(SYSTEM_USER_ID);
                teacher1.setUpdateBy(SYSTEM_USER_ID);

                Role student1 = Role.builder()
                        .roleName("STUDENT")
                        .typeRole(RoleType.SCHOOL)
                        .description("Học sinh trong trường học")
                        .school(school1)
                        .build();
                student1.setCreateBy(SYSTEM_USER_ID);
                student1.setUpdateBy(SYSTEM_USER_ID);

                roles.add(schoolAdmin1);
                roles.add(teacher1);
                roles.add(student1);
            }

            // School Roles - School 2
            School school2 = schoolRepository.findByCode("THCS002").orElse(null);
            if (school2 != null) {
                Role schoolAdmin2 = Role.builder()
                        .roleName("SCHOOL_ADMIN")
                        .typeRole(RoleType.SCHOOL)
                        .description("Quản trị viên trường học")
                        .school(school2)
                        .build();
                schoolAdmin2.setCreateBy(SYSTEM_USER_ID);
                schoolAdmin2.setUpdateBy(SYSTEM_USER_ID);

                Role teacher2 = Role.builder()
                        .roleName("TEACHER")
                        .typeRole(RoleType.SCHOOL)
                        .description("Giáo viên trong trường học")
                        .school(school2)
                        .build();
                teacher2.setCreateBy(SYSTEM_USER_ID);
                teacher2.setUpdateBy(SYSTEM_USER_ID);

                Role student2 = Role.builder()
                        .roleName("STUDENT")
                        .typeRole(RoleType.SCHOOL)
                        .description("Học sinh trong trường học")
                        .school(school2)
                        .build();
                student2.setCreateBy(SYSTEM_USER_ID);
                student2.setUpdateBy(SYSTEM_USER_ID);

                roles.add(schoolAdmin2);
                roles.add(teacher2);
                roles.add(student2);
            }

            roleRepository.saveAll(roles);
            log.info("✅ Đã seed {} role", roles.size());
        } else {
            log.info("ℹ️ Role đã tồn tại, bỏ qua seed dữ liệu");
        }
    }

    private void seedUsers() {
        if (userRepository.count() == 0) {
            List<User> users = new ArrayList<>();

            // Provider Users
            Role systemAdminRole = roleRepository.findByRoleName("SYSTEM_ADMIN").orElse(null);
            Role systemStaffRole = roleRepository.findByRoleName("SYSTEM_STAFF").orElse(null);

            if (systemAdminRole != null) {
                User adminUser = User.builder()
                        .fullName("Admin Hệ Thống")
                        .gender(Gender.MALE)
                        .birthYear(LocalDateTime.of(1980, 5, 15, 0, 0))
                        .address("10 Đường Trần Phú, Hà Nội")
                        .phoneNumber("0912345678")
                        .email("admin@system.com")
                        .password(passwordEncoder.encode("Admin123"))
                        .isActive(true)
                        .scope(UserScope.PROVIDER)
                        .school(null)
                        .role(systemAdminRole)
                        .build();
                adminUser.setCreateBy(SYSTEM_USER_ID);
                adminUser.setUpdateBy(SYSTEM_USER_ID);
                users.add(adminUser);
            }

            if (systemStaffRole != null) {
                User staffUser = User.builder()
                        .fullName("Nhân Viên Hệ Thống")
                        .gender(Gender.FEMALE)
                        .birthYear(LocalDateTime.of(1990, 8, 20, 0, 0))
                        .address("20 Đường Lý Thường Kiệt, Hà Nội")
                        .phoneNumber("0923456789")
                        .email("staff@system.com")
                        .password(passwordEncoder.encode("Staff123"))
                        .isActive(true)
                        .scope(UserScope.PROVIDER)
                        .school(null)
                        .role(systemStaffRole)
                        .build();
                staffUser.setCreateBy(SYSTEM_USER_ID);
                staffUser.setUpdateBy(SYSTEM_USER_ID);
                users.add(staffUser);
            }

            // School 1 Users
            School school1 = schoolRepository.findByCode("TH001").orElse(null);
            if (school1 != null) {
                Role schoolAdminRole1 = roleRepository.findByRoleNameAndSchoolId("SCHOOL_ADMIN", school1.getId()).orElse(null);
                Role teacherRole1 = roleRepository.findByRoleNameAndSchoolId("TEACHER", school1.getId()).orElse(null);
                Role studentRole1 = roleRepository.findByRoleNameAndSchoolId("STUDENT", school1.getId()).orElse(null);

                if (schoolAdminRole1 != null) {
                    User schoolAdmin1 = User.builder()
                            .fullName("Nguyễn Văn An")
                            .gender(Gender.MALE)
                            .birthYear(LocalDateTime.of(1975, 3, 10, 0, 0))
                            .address("123 Đường Nguyễn Du, Hà Nội")
                            .phoneNumber("0934567890")
                            .email("an.nguyen@school1.edu.vn")
                            .password(passwordEncoder.encode("Admin123"))
                            .isActive(true)
                            .scope(UserScope.SCHOOL)
                            .school(school1)
                            .role(schoolAdminRole1)
                            .build();
                    schoolAdmin1.setCreateBy(SYSTEM_USER_ID);
                    schoolAdmin1.setUpdateBy(SYSTEM_USER_ID);
                    users.add(schoolAdmin1);
                }

                if (teacherRole1 != null) {
                    User teacher1 = User.builder()
                            .fullName("Trần Thị Lan")
                            .gender(Gender.FEMALE)
                            .birthYear(LocalDateTime.of(1985, 7, 25, 0, 0))
                            .address("456 Đường Nguyễn Du, Hà Nội")
                            .phoneNumber("0945678901")
                            .email("lan.tran@school1.edu.vn")
                            .password(passwordEncoder.encode("Teacher123"))
                            .isActive(true)
                            .scope(UserScope.SCHOOL)
                            .school(school1)
                            .role(teacherRole1)
                            .build();
                    teacher1.setCreateBy(SYSTEM_USER_ID);
                    teacher1.setUpdateBy(SYSTEM_USER_ID);
                    users.add(teacher1);
                }

                if (studentRole1 != null) {
                    User student1 = User.builder()
                            .fullName("Lê Văn Hùng")
                            .gender(Gender.MALE)
                            .birthYear(LocalDateTime.of(2010, 9, 15, 0, 0))
                            .address("789 Đường Nguyễn Du, Hà Nội")
                            .phoneNumber("0956789012")
                            .email("hung.le@school1.edu.vn")
                            .password(passwordEncoder.encode("Student123"))
                            .isActive(true)
                            .scope(UserScope.SCHOOL)
                            .school(school1)
                            .role(studentRole1)
                            .build();
                    student1.setCreateBy(SYSTEM_USER_ID);
                    student1.setUpdateBy(SYSTEM_USER_ID);
                    users.add(student1);
                }
            }

            // School 2 Users
            School school2 = schoolRepository.findByCode("THCS002").orElse(null);
            if (school2 != null) {
                Role schoolAdminRole2 = roleRepository.findByRoleNameAndSchoolId("SCHOOL_ADMIN", school2.getId()).orElse(null);
                Role teacherRole2 = roleRepository.findByRoleNameAndSchoolId("TEACHER", school2.getId()).orElse(null);
                Role studentRole2 = roleRepository.findByRoleNameAndSchoolId("STUDENT", school2.getId()).orElse(null);

                if (schoolAdminRole2 != null) {
                    User schoolAdmin2 = User.builder()
                            .fullName("Trần Thị Bình")
                            .gender(Gender.FEMALE)
                            .birthYear(LocalDateTime.of(1978, 11, 30, 0, 0))
                            .address("123 Đường Lê Lợi, Hà Nội")
                            .phoneNumber("0967890123")
                            .email("binh.tran@school2.edu.vn")
                            .password(passwordEncoder.encode("Admin123"))
                            .isActive(true)
                            .scope(UserScope.SCHOOL)
                            .school(school2)
                            .role(schoolAdminRole2)
                            .build();
                    schoolAdmin2.setCreateBy(SYSTEM_USER_ID);
                    schoolAdmin2.setUpdateBy(SYSTEM_USER_ID);
                    users.add(schoolAdmin2);
                }

                if (teacherRole2 != null) {
                    User teacher2 = User.builder()
                            .fullName("Phạm Văn Đức")
                            .gender(Gender.MALE)
                            .birthYear(LocalDateTime.of(1988, 4, 12, 0, 0))
                            .address("456 Đường Lê Lợi, Hà Nội")
                            .phoneNumber("0978901234")
                            .email("duc.pham@school2.edu.vn")
                            .password(passwordEncoder.encode("Teacher123"))
                            .isActive(true)
                            .scope(UserScope.SCHOOL)
                            .school(school2)
                            .role(teacherRole2)
                            .build();
                    teacher2.setCreateBy(SYSTEM_USER_ID);
                    teacher2.setUpdateBy(SYSTEM_USER_ID);
                    users.add(teacher2);
                }

                if (studentRole2 != null) {
                    User student2 = User.builder()
                            .fullName("Hoàng Thị Mai")
                            .gender(Gender.FEMALE)
                            .birthYear(LocalDateTime.of(2011, 2, 28, 0, 0))
                            .address("789 Đường Lê Lợi, Hà Nội")
                            .phoneNumber("0989012345")
                            .email("mai.hoang@school2.edu.vn")
                            .password(passwordEncoder.encode("Student123"))
                            .isActive(true)
                            .scope(UserScope.SCHOOL)
                            .school(school2)
                            .role(studentRole2)
                            .build();
                    student2.setCreateBy(SYSTEM_USER_ID);
                    student2.setUpdateBy(SYSTEM_USER_ID);
                    users.add(student2);
                }
            }

            userRepository.saveAll(users);
            log.info("✅ Đã seed {} người dùng", users.size());
        } else {
            log.info("ℹ️ Người dùng đã tồn tại, bỏ qua seed dữ liệu");
        }
    }
}