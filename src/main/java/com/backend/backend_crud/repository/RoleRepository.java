package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
        /**
         * Tìm role theo tên
         */
        Optional<Role> findByRoleName(String roleName);

        /**
         * Tìm role theo tên và school (dùng cho DataSeeder)
         */
        Optional<Role> findByRoleNameAndSchool(String roleName, School school);

        /**
         * Tìm roles có school = null (system roles)
         */
        List<Role> findBySchoolIsNull();

        /**
         * Tìm roles theo typeRole và school = null (system-level roles của một loại cụ
         * thể)
         */
        List<Role> findByTypeRoleAndSchoolIsNull(RoleType typeRole);

        /**
         * Tìm roles theo schoolId
         */
        List<Role> findBySchoolId(Long schoolId);

        /**
         * Kiểm tra roleName đã tồn tại chưa
         */
        boolean existsByRoleName(String roleName);

        /**
         * Kiểm tra roleName đã tồn tại chưa (trừ id hiện tại)
         */
        boolean existsByRoleNameAndIdNot(String roleName, Long id);

        /**
         * Kiểm tra roleName đã tồn tại chưa với cùng typeRole và schoolId
         * Với PROVIDER: schoolId = null
         * Với SCHOOL: schoolId phải match
         */
        boolean existsByRoleNameAndTypeRoleAndSchoolId(String roleName, RoleType typeRole, Long schoolId);

        /**
         * Kiểm tra roleName đã tồn tại chưa với cùng typeRole và schoolId (trừ id hiện
         * tại)
         */
        boolean existsByRoleNameAndTypeRoleAndSchoolIdAndIdNot(String roleName, RoleType typeRole, Long schoolId,
                        Long id);
}
