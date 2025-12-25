package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {
    Optional<School> findByCode(String code);

    Page<School> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // --- Check cho CREATE ---
    boolean existsByCode(String code);
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    boolean existsByHotline(String hotline);

    // --- Check cho UPDATE (Trừ ID hiện tại ra) ---
    boolean existsByCodeAndIdNot(String code, Long id);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByHotlineAndIdNot(String hotline, Long id);
}
