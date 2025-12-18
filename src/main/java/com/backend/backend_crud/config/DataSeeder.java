package com.backend.backend_crud.config;

import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        seedRoles();
    }

    private void seedRoles() {
        if (roleRepository.count() == 0) {
            Role adminProvider = Role.builder()
                    .roleName("ADMIN_PROVIDER")
                    .typeRole(RoleType.PROVIDER)
                    .description("Admin Hệ Thống")
                    .school(null)
                    .build();

            Role adminSchool = Role.builder()
                    .roleName("ADMIN_SCHOOL")
                    .typeRole(RoleType.SCHOOL)
                    .description("Admin Trường Học")
                    .school(null)
                    .build();

            roleRepository.save(adminProvider);
            roleRepository.save(adminSchool);

            log.info("✅ Đã seed 2 role mặc định: ADMIN_PROVIDER, ADMIN_SCHOOL");
        } else {
            log.info("ℹ️ Role đã tồn tại, bỏ qua seed dữ liệu");
        }
    }
}