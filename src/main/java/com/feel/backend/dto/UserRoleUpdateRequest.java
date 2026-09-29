package com.feel.backend.dto;

import com.feel.backend.entity.UserRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRoleUpdateRequest {

    private UserRole role;

    /** 선택 */
    private String reason;
}
