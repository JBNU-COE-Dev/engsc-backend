package com.feel.backend.dto;

import com.feel.backend.entity.UserStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStatusUpdateRequest {

    /** SUSPENDED: 정지, ACTIVE: 정지 해제 */
    private UserStatus status;

    /** 정지 시 필수, 해제 시 선택 */
    private String reason;

    /** 정지 해제 예정 시각. null이면 영구 정지 (SUSPENDED일 때만 사용) */
    private LocalDateTime suspendedUntil;
}
