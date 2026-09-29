package com.feel.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

/**
 * 일반 사용자 (Google 웹메일 @jbnu.ac.kr 로그인)
 * 비밀번호 없음, 이메일·닉네임만 저장
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 100)
    private String nickname;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기존 행이 있는 테이블에 ddl-auto=update로 컬럼을 추가하므로 DB 기본값 필요
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'USER'")
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @ColumnDefault("'ACTIVE'")
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /** 정지 해제 예정 시각. SUSPENDED인데 null이면 영구 정지 */
    @Column(name = "suspended_until")
    private LocalDateTime suspendedUntil;

    @Column(name = "suspend_reason", length = 500)
    private String suspendReason;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /** 정지 기간이 지났으면 별도 해제 처리 없이 정상으로 본다. */
    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED
                && (suspendedUntil == null || suspendedUntil.isAfter(LocalDateTime.now()));
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
