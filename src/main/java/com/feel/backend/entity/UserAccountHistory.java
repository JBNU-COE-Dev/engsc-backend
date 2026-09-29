package com.feel.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 회원 권한·상태 변경 이력 (누가, 언제, 왜)
 */
@Entity
@Table(name = "user_account_history", indexes = @Index(name = "idx_user_account_history_user_id", columnList = "user_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** users.id */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserAccountAction action;

    @Column(length = 500)
    private String reason;

    /** SUSPEND일 때 정지 해제 예정 시각. null이면 영구 정지 */
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    /** 처리한 관리자 (admin_users.username 또는 관리자 회원 이메일) */
    @Column(name = "performed_by", nullable = false, length = 255)
    private String performedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
