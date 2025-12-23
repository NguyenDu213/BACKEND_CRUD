package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.User;
import com.backend.backend_crud.entity.UserScope;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface UserRepository extends JpaRepository<User, Long> {
    Long countByRoleId(Long roleId);
    List<User> findBySchoolId(Long schoolId);
    Optional<User> findByEmail(String email);

    /**
     * Batch query để lấy user count cho nhiều roles cùng lúc
     * Trả về Map<roleId, userCount> để tránh N+1 query problem
     */
    @Query("SELECT u.role.id, COUNT(u) FROM User u WHERE u.role.id IN :roleIds GROUP BY u.role.id")
    List<Object[]> countUsersByRoleIds(@Param("roleIds") List<Long> roleIds);

    /**
     * Helper method để convert query result thành Map
     */
    default Map<Long, Long> getUserCountsByRoleIds(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return countUsersByRoleIds(roleIds).stream()
                .collect(Collectors.toMap(
                        result -> ((Long) result[0]),
                        result -> {
                            Object count = result[1];
                            if (count instanceof Long) {
                                return (Long) count;
                            } else if (count instanceof Integer) {
                                return ((Integer) count).longValue();
                            } else if (count instanceof Number) {
                                return ((Number) count).longValue();
                            }
                            return 0L;
                        }));
    }

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
