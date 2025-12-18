package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
