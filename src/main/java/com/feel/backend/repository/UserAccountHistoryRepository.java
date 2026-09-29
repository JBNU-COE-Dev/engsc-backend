package com.feel.backend.repository;

import com.feel.backend.entity.UserAccountHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountHistoryRepository extends JpaRepository<UserAccountHistory, Long> {

    Page<UserAccountHistory> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);
}
