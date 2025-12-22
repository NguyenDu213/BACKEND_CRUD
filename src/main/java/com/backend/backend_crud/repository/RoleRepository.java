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
    
    @Query("SELECT r FROM Role r WHERE r.school IS NULL OR r.school.id = :schoolId")
    List<Role> findBySchoolIdOrSystemRoles(@Param("schoolId") Long schoolId);
    
    @Query("SELECT r FROM Role r WHERE r.typeRole = :typeRole AND (r.school IS NULL OR r.school.id = :schoolId)")
    List<Role> findByTypeRoleAndSchoolIdOrSystem(@Param("typeRole") RoleType typeRole, @Param("schoolId") Long schoolId);
}
