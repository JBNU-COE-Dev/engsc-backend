package com.feel.backend.repository;

import com.feel.backend.entity.User;
import com.feel.backend.entity.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    /**
     * status는 실제 적용 상태 기준: 정지 기간이 지난 SUSPENDED는 ACTIVE로 취급.
     * statusFilter: null(전체) | "ACTIVE" | "SUSPENDED"
     */
    @Query("""
        SELECT u FROM User u
        WHERE (:search IS NULL OR :search = ''
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:role IS NULL OR u.role = :role)
          AND (:statusFilter IS NULL
           OR (:statusFilter = 'SUSPENDED'
               AND u.status = com.feel.backend.entity.UserStatus.SUSPENDED
               AND (u.suspendedUntil IS NULL OR u.suspendedUntil > :now))
           OR (:statusFilter = 'ACTIVE'
               AND (u.status = com.feel.backend.entity.UserStatus.ACTIVE
                 OR u.suspendedUntil <= :now)))
        ORDER BY u.createdAt DESC
        """)
    Page<User> search(@Param("search") String search,
                      @Param("role") UserRole role,
                      @Param("statusFilter") String statusFilter,
                      @Param("now") LocalDateTime now,
                      Pageable pageable);
}
