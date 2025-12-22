package com.backend.backend_crud.repository;

import com.backend.backend_crud.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface UserRepository extends JpaRepository<User, Long> {
    Long countByRoleId(Long roleId);

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
}
