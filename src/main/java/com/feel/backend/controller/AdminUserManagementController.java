package com.feel.backend.controller;

import com.feel.backend.dto.ActivityPostResponseDto;
import com.feel.backend.dto.AdminUserResponseDto;
import com.feel.backend.dto.ErrorResponse;
import com.feel.backend.dto.UserAccountHistoryDto;
import com.feel.backend.dto.UserRoleUpdateRequest;
import com.feel.backend.dto.UserStatusUpdateRequest;
import com.feel.backend.entity.UserRole;
import com.feel.backend.entity.UserStatus;
import com.feel.backend.service.AdminUserService;
import com.feel.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserManagementController {

    private final AdminUserService adminUserService;
    private final AuthService authService;

    @GetMapping("/users")
    public ResponseEntity<?> getUsers(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status
    ) {
        try {
            authService.requireAdmin(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        Page<AdminUserResponseDto> users = adminUserService.getUsers(search, role, status, page, size);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId
    ) {
        try {
            authService.requireAdmin(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            return ResponseEntity.ok(adminUserService.getUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<?> getUserPosts(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            authService.requireAdmin(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            Page<ActivityPostResponseDto> posts = adminUserService.getUserPosts(userId, page, size);
            return ResponseEntity.ok(posts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
    }

    @GetMapping("/users/{userId}/history")
    public ResponseEntity<?> getUserHistory(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        try {
            authService.requireAdmin(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            Page<UserAccountHistoryDto> history = adminUserService.getHistory(userId, page, size);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
    }

    /** 계정 정지 / 정지 해제 */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody UserStatusUpdateRequest request
    ) {
        String performedBy;
        try {
            performedBy = authService.getUsernameFromToken(authService.requireAdmin(authHeader));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            return ResponseEntity.ok(adminUserService.updateStatus(userId, request, performedBy));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
    }

    /** 관리자 권한 부여 / 해제 */
    @PatchMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long userId,
            @RequestBody UserRoleUpdateRequest request
    ) {
        String performedBy;
        try {
            performedBy = authService.getUsernameFromToken(authService.requireAdmin(authHeader));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        try {
            return ResponseEntity.ok(adminUserService.updateRole(userId, request, performedBy));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
    }
}
