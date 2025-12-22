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
        Optional<Role> findByRoleName(String roleName);

        Optional<Role> findByRoleNameAndSchool(String roleName, School school);

        List<Role> findByTypeRole(RoleType typeRole);

        List<Role> findBySchool(School school);

        List<Role> findByTypeRoleAndSchool(RoleType typeRole, School school);

        // Tìm roles có school = null (system roles)
        List<Role> findBySchoolIsNull();

        // Tìm roles theo schoolId
        List<Role> findBySchoolId(Long schoolId);

        // Kiểm tra roleName đã tồn tại chưa
        boolean existsByRoleName(String roleName);

        // Kiểm tra roleName đã tồn tại chưa (trừ id hiện tại)
        boolean existsByRoleNameAndIdNot(String roleName, Long id);

        @Query("SELECT r FROM Role r WHERE r.school IS NULL OR r.school.id = :schoolId")
        List<Role> findBySchoolIdOrSystemRoles(@Param("schoolId") Long schoolId);

        @Query("SELECT r FROM Role r WHERE r.typeRole = :typeRole AND (r.school IS NULL OR r.school.id = :schoolId)")
        List<Role> findByTypeRoleAndSchoolIdOrSystem(@Param("typeRole") RoleType typeRole,
                        @Param("schoolId") Long schoolId);

        // Tìm kiếm các role hệ thống (school IS NULL) kèm theo số lượng User
        @Query("SELECT r, (SELECT COUNT(u) FROM User u WHERE u.role.id = r.id) " +
                        "FROM Role r WHERE r.school IS NULL")
        List<Object[]> findBySchoolIsNullWithCount();

        // Tìm kiếm các role của một trường học cụ thể kèm theo số lượng User
        @Query("SELECT r, (SELECT COUNT(u) FROM User u WHERE u.role.id = r.id) " +
                        "FROM Role r WHERE r.school.id = :schoolId")
        List<Object[]> findBySchoolIdWithCount(@Param("schoolId") Long schoolId);
}
