package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.entity.UserScope;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Long countByRoleId(Long roleId);

    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);


    @Query("SELECT u.scope FROM User u WHERE u.id = :id")
    UserScope findScopeByUserId(@Param("id") Long id);
    @Query("SELECT r.roleName FROM User u JOIN u.role r WHERE u.id = :userId")
    String findRoleNameByUserId(@Param("userId") Long userId);
    @Query("SELECT u FROM User u WHERE u.school.id IS NULL")
    List<User> findAllSystemUsers();
    @Query("SELECT u FROM User u WHERE u.school.id = :id")
    List<User> findAllSchoolUsers(@Param("id") Long id);
    @Query("SELECT u.school.id FROM User u WHERE u.id = :userId")
    Long findSchoolIdByUserId(@Param("userId") Long userId);
    @Query("""
    SELECT u
    FROM User u
    WHERE u.scope = :scope
          AND (
                  :keyword IS NULL
                  OR :keyword = ''
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              )
""")
    List<User> searchByName(@Param("keyword") String keyword, @Param("scope") UserScope scope);
    @Query("""
                SELECT u
                FROM User u
                WHERE u.school.id = :schoolId
                  AND u.scope = :scope
                  AND (
                  :keyword IS NULL
                  OR :keyword = ''
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
            """)
    List<User> searchBySchoolAndName(
            @Param("schoolId") Long schoolId,
            @Param("scope") UserScope scope,
            @Param("keyword") String keyword
    );
}
