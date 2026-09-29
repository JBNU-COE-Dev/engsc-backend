package com.feel.backend.dto;

import com.feel.backend.entity.UserAccountAction;
import com.feel.backend.entity.UserAccountHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccountHistoryDto {

    private Long id;
    private UserAccountAction action;
    private String reason;
    private LocalDateTime suspendedUntil;
    private String performedBy;
    private LocalDateTime createdAt;

    public static UserAccountHistoryDto fromEntity(UserAccountHistory history) {
        return UserAccountHistoryDto.builder()
                .id(history.getId())
                .action(history.getAction())
                .reason(history.getReason())
                .suspendedUntil(history.getSuspendedUntil())
                .performedBy(history.getPerformedBy())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
