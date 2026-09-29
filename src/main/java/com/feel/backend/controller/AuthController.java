package com.feel.backend.controller;

import com.feel.backend.dto.ErrorResponse;
import com.feel.backend.dto.GoogleLoginRequest;
import com.feel.backend.dto.LoginRequest;
import com.feel.backend.dto.SignupRequest;
import com.feel.backend.dto.LoginResponse;
import com.feel.backend.dto.VerifyResponse;
import com.feel.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            LoginResponse response = authService.googleLogin(request.getIdToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /** 관리자 페이지 Google 로그인 (관리자 권한을 받은 회원만) */
    @PostMapping("/admin/google")
    public ResponseEntity<?> adminGoogleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        try {
            LoginResponse response = authService.adminGoogleLogin(request.getIdToken());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            LoginResponse response = authService.completeSignup(request.getIdToken(), request.getNickname());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request.getUsername(), request.getPassword());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verify(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("유효하지 않은 토큰입니다.")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        String token = authHeader.substring(7); // "Bearer " 제거

        try {
            if (authService.validateToken(token)) {
                var verifyResult = authService.verifyAndGetUserInfo(token);
                VerifyResponse response = VerifyResponse.builder()
                        .valid(true)
                        .id(verifyResult.id())
                        .username(verifyResult.username())
                        .nickname(verifyResult.nickname())
                        .role(verifyResult.role().name())
                        .build();
                return ResponseEntity.ok(response);
            } else {
                ErrorResponse errorResponse = ErrorResponse.builder()
                        .message("유효하지 않은 토큰입니다.")
                        .build();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
        } catch (Exception e) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .message("유효하지 않은 토큰입니다.")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            authService.logout(token);
        }

        return ResponseEntity.ok(ErrorResponse.builder()
                .message("로그아웃되었습니다.")
                .build());
    }
}
