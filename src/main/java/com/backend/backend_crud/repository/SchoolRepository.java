package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}
