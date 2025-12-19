package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Long countByRoleId(Long roleId);
    
    Optional<User> findByEmail(String email);
}
