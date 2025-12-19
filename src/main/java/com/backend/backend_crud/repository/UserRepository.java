package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Long countByRoleId(Long roleId);

    boolean existsByEmail(String email);
}
