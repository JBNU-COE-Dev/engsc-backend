package com.feel.backend.entity;

/**
 * 회원 계정 변경 이력 종류
 */
public enum UserAccountAction {
    SUSPEND,        // 정지
    UNSUSPEND,      // 정지 해제
    GRANT_ADMIN,    // 관리자 권한 부여
    REVOKE_ADMIN    // 관리자 권한 해제
}
