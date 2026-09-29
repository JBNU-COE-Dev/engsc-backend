package com.feel.backend.dto;

import com.feel.backend.entity.User;
import com.feel.backend.entity.UserRole;
import com.feel.backend.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponseDto {

    private Long id;
    private String email;
    private String nickname;
    private LocalDateTime createdAt;
    private long postCount;
    private UserRole role;
    /** 실제 적용 상태. 정지 기간이 지났으면 ACTIVE */
    private UserStatus status;
    /** 정지 중일 때만 값 있음. null이면 영구 정지 */
    private LocalDateTime suspendedUntil;
    private String suspendReason;

    public static AdminUserResponseDto fromEntity(User user, long postCount) {
        boolean suspended = user.isSuspended();
        return AdminUserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .postCount(postCount)
                .role(user.getRole())
                .status(suspended ? UserStatus.SUSPENDED : UserStatus.ACTIVE)
                .suspendedUntil(suspended ? user.getSuspendedUntil() : null)
                .suspendReason(suspended ? user.getSuspendReason() : null)
                .build();
    }
}
