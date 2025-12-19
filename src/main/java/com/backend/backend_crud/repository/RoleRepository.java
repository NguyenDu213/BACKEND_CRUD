package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
    Optional<Role> findByRoleNameAndSchoolId(String roleName, Long schoolId);
}
