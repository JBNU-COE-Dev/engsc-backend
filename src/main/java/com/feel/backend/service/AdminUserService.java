package com.feel.backend.service;

import com.feel.backend.dto.ActivityPostResponseDto;
import com.feel.backend.dto.AdminUserResponseDto;
import com.feel.backend.dto.UserAccountHistoryDto;
import com.feel.backend.dto.UserRoleUpdateRequest;
import com.feel.backend.dto.UserStatusUpdateRequest;
import com.feel.backend.entity.User;
import com.feel.backend.entity.UserAccountAction;
import com.feel.backend.entity.UserAccountHistory;
import com.feel.backend.entity.UserRole;
import com.feel.backend.entity.UserStatus;
import com.feel.backend.repository.ActivityPostRepository;
import com.feel.backend.repository.UserAccountHistoryRepository;
import com.feel.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUserService {

    private static final int REASON_MAX_LENGTH = 500;

    private final UserRepository userRepository;
    private final ActivityPostRepository activityPostRepository;
    private final ActivityPostService activityPostService;
    private final UserAccountHistoryRepository userAccountHistoryRepository;

    public Page<AdminUserResponseDto> getUsers(String search, UserRole role, UserStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String normalized = (search == null || search.isBlank()) ? null : search.trim();
        String statusFilter = status != null ? status.name() : null;
        return userRepository.search(normalized, role, statusFilter, LocalDateTime.now(), pageable)
                .map(this::toDto);
    }

    public AdminUserResponseDto getUser(Long userId) {
        return toDto(findUser(userId));
    }

    public Page<ActivityPostResponseDto> getUserPosts(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("회원을 찾을 수 없습니다.");
        }
        return activityPostService.getByAuthorId(userId, page, size);
    }

    public Page<UserAccountHistoryDto> getHistory(Long userId, int page, int size) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("회원을 찾을 수 없습니다.");
        }
        return userAccountHistoryRepository
                .findByUserIdOrderByCreatedAtDescIdDesc(userId, PageRequest.of(page, size))
                .map(UserAccountHistoryDto::fromEntity);
    }

    /**
     * 계정 정지 / 정지 해제. 잘못된 요청은 IllegalArgumentException.
     *
     * @param performedBy 처리한 관리자 (토큰 subject)
     */
    @Transactional
    public AdminUserResponseDto updateStatus(Long userId, UserStatusUpdateRequest request, String performedBy) {
        if (request == null || request.getStatus() == null) {
            throw new IllegalArgumentException("변경할 상태를 지정해주세요.");
        }
        User user = findUser(userId);
        assertNotSelf(user, performedBy);
        String reason = normalizeReason(request.getReason());

        UserAccountHistory.UserAccountHistoryBuilder history = UserAccountHistory.builder()
                .userId(user.getId())
                .reason(reason)
                .performedBy(performedBy);

        if (request.getStatus() == UserStatus.SUSPENDED) {
            if (reason == null) {
                throw new IllegalArgumentException("정지 사유를 입력해주세요.");
            }
            LocalDateTime until = request.getSuspendedUntil();
            if (until != null && !until.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("정지 해제일은 현재 시각 이후여야 합니다.");
            }
            user.setStatus(UserStatus.SUSPENDED);
            user.setSuspendedUntil(until);
            user.setSuspendReason(reason);
            history.action(UserAccountAction.SUSPEND).suspendedUntil(until);
        } else {
            if (!user.isSuspended()) {
                throw new IllegalArgumentException("정지 상태인 회원이 아닙니다.");
            }
            user.setStatus(UserStatus.ACTIVE);
            user.setSuspendedUntil(null);
            user.setSuspendReason(null);
            history.action(UserAccountAction.UNSUSPEND);
        }

        userAccountHistoryRepository.save(history.build());
        return toDto(user);
    }

    /**
     * 관리자 권한 부여 / 해제. ADMIN이면 admin/ 에 Google 계정으로 로그인 가능.
     */
    @Transactional
    public AdminUserResponseDto updateRole(Long userId, UserRoleUpdateRequest request, String performedBy) {
        if (request == null || request.getRole() == null) {
            throw new IllegalArgumentException("변경할 권한을 지정해주세요.");
        }
        User user = findUser(userId);
        assertNotSelf(user, performedBy);
        UserRole role = request.getRole();
        if (user.getRole() == role) {
            throw new IllegalArgumentException("이미 해당 권한을 가진 회원입니다.");
        }
        if (role == UserRole.ADMIN && user.isSuspended()) {
            throw new IllegalArgumentException("정지된 회원에게는 관리자 권한을 부여할 수 없습니다.");
        }

        user.setRole(role);
        userAccountHistoryRepository.save(UserAccountHistory.builder()
                .userId(user.getId())
                .action(role == UserRole.ADMIN ? UserAccountAction.GRANT_ADMIN : UserAccountAction.REVOKE_ADMIN)
                .reason(normalizeReason(request.getReason()))
                .performedBy(performedBy)
                .build());
        return toDto(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("회원을 찾을 수 없습니다."));
    }

    private AdminUserResponseDto toDto(User user) {
        return AdminUserResponseDto.fromEntity(user, activityPostRepository.countByAuthorId(user.getId()));
    }

    /** 관리자 회원이 자기 권한을 해제하거나 자기 계정을 정지해 잠기는 것을 막는다. */
    private void assertNotSelf(User user, String performedBy) {
        if (user.getEmail().equalsIgnoreCase(performedBy)) {
            throw new IllegalArgumentException("본인 계정의 권한이나 상태는 변경할 수 없습니다.");
        }
    }

    private String normalizeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return null;
        }
        String trimmed = reason.trim();
        if (trimmed.length() > REASON_MAX_LENGTH) {
            throw new IllegalArgumentException("사유는 " + REASON_MAX_LENGTH + "자 이하로 입력해주세요.");
        }
        return trimmed;
    }
}
