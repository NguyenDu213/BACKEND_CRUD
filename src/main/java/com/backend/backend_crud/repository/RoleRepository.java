package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

        @Query("SELECT r.typeRole FROM Role r WHERE r.id = :roleId")
        RoleType findTypeRoleByRoleId(@Param("roleId") Long roleId);
}
