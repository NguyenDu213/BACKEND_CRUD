package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.Role;
import com.backend.backend_crud.entity.RoleType;
import com.backend.backend_crud.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

        Optional<Role> findByRoleName(String roleName);

        Optional<Role> findByRoleNameAndSchool(String roleName, School school);

        @Query("SELECT r FROM Role r WHERE " +
                        "(:schoolId IS NULL OR r.school.id = :schoolId) AND " +
                        "(:keyword IS NULL OR LOWER(r.roleName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
                        "(:type IS NULL OR r.typeRole = :type) AND " +
                        "r.roleName != 'SYSTEM_ADMIN' AND r.roleName != 'SCHOOL_ADMIN'")
        Page<Role> searchRoles(@Param("schoolId") Long schoolId,
                        @Param("keyword") String keyword,
                        @Param("type") RoleType type,
                        Pageable pageable);

        @Query("SELECT r FROM Role r WHERE r.school.id = :schoolId AND r.roleName != 'SCHOOL_ADMIN'")
        Page<Role> findBySchoolId(@Param("schoolId") Long schoolId, Pageable pageable);

        @Query("SELECT r FROM Role r WHERE r.school IS NULL AND r.typeRole = :typeRole AND r.roleName != 'SYSTEM_ADMIN'")
        Page<Role> findBySchoolIsNull(@Param("typeRole") RoleType typeRole, Pageable pageable);

        @Query("SELECT COUNT(r) > 0 FROM Role r WHERE " +
                        "r.roleName = :roleName AND " +
                        "r.typeRole = :typeRole AND " +
                        "((:schoolId IS NULL AND r.school IS NULL) OR (r.school.id = :schoolId))")
        boolean existsDuplicateRole(@Param("roleName") String roleName,
                        @Param("typeRole") RoleType typeRole,
                        @Param("schoolId") Long schoolId);

        @Query("SELECT COUNT(r) > 0 FROM Role r WHERE " +
                        "r.roleName = :roleName AND " +
                        "r.typeRole = :typeRole AND " +
                        "((:schoolId IS NULL AND r.school IS NULL) OR (r.school.id = :schoolId)) AND " +
                        "r.id != :id")
        boolean existsDuplicateRoleForUpdate(@Param("roleName") String roleName,
                        @Param("typeRole") RoleType typeRole,
                        @Param("schoolId") Long schoolId,
                        @Param("id") Long id);

        @Query("SELECT r.typeRole FROM Role r WHERE r.id = :roleId")
        RoleType findTypeRoleByRoleId(@Param("roleId") Long roleId);
}
