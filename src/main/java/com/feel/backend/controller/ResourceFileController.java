package com.feel.backend.controller;

import com.feel.backend.dto.ErrorResponse;
import com.feel.backend.dto.ResourceFileDto;
import com.feel.backend.service.AuthService;
import com.feel.backend.service.FinanceReportService;
import com.feel.backend.service.ResourceFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceFileController {

    private final ResourceFileService resourceFileService;
    private final FinanceReportService financeReportService;
    private final AuthService authService;

    /**
     * 파일 업로드
     * POST /api/resources/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam MultipartFile file
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        ResourceFileDto.Response response = resourceFileService.uploadFile(
                category, title, description, year, month, eventDate, file
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 여러 파일 업로드
     * POST /api/resources/upload-multiple
     */
    @PostMapping("/upload-multiple")
    public ResponseEntity<?> uploadMultipleFiles(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String category,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam List<MultipartFile> files
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        List<ResourceFileDto.Response> responses = files.stream()
                .map(file -> resourceFileService.uploadFile(category, title, description, year, month, eventDate, file))
                .toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    /**
     * 카테고리별 파일 목록 조회 (페이징)
     * GET /api/resources?category=inspection&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<ResourceFileDto.Response>> getFilesByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ResourceFileDto.Response> files = resourceFileService.getFilesByCategory(category, page, size);
        return ResponseEntity.ok(files);
    }

    /**
     * 카테고리별 전체 파일 목록 조회
     * GET /api/resources/all?category=inspection
     */
    @GetMapping("/all")
    public ResponseEntity<List<ResourceFileDto.Response>> getAllFilesByCategory(
            @RequestParam String category
    ) {
        List<ResourceFileDto.Response> files = resourceFileService.getAllFilesByCategory(category);
        return ResponseEntity.ok(files);
    }

    /**
     * 특정 파일 조회
     * GET /api/resources/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResourceFileDto.Response> getFileById(@PathVariable Long id) {
        ResourceFileDto.Response file = resourceFileService.getFileById(id);
        return ResponseEntity.ok(file);
    }

    /**
     * 리소스 메타 수정 (제목, 설명, 행사일)
     * PATCH /api/resources/{id}
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateFileMeta(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id,
            @RequestBody ResourceFileDto.UpdateRequest request
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        ResourceFileDto.Response response = resourceFileService.updateFileMeta(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 파일 삭제
     * DELETE /api/resources/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFile(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Long id
    ) {
        try {
            authService.validateAuthHeader(authHeader);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.builder().message(e.getMessage()).build());
        }
        resourceFileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 카테고리별 파일 개수 조회
     * GET /api/resources/count?category=inspection
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getCountByCategory(@RequestParam String category) {
        long count = resourceFileService.getCountByCategory(category);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * 전체 카테고리 통계
     * GET /api/resources/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(Map.of(
                "inspection", resourceFileService.getCountByCategory("inspection"),
                // 회계내역은 finance_reports 테이블에 별도 저장됨
                "finance", financeReportService.getTotalCount(),
                "gallery", resourceFileService.getCountByCategory("gallery"),
                "study-support", resourceFileService.getCountByCategory("study-support")
        ));
    }

    /**
     * 연도/월별 파일 목록 조회
     * GET /api/resources/by-period?category=inspection&year=2026&month=1
     */
    @GetMapping("/by-period")
    public ResponseEntity<List<ResourceFileDto.Response>> getFilesByPeriod(
            @RequestParam String category,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<ResourceFileDto.Response> files;
        if (month != null) {
            files = resourceFileService.getFilesByCategoryAndYearMonth(category, year, month);
        } else {
            files = resourceFileService.getFilesByCategoryAndYear(category, year);
        }
        return ResponseEntity.ok(files);
    }

    /**
     * 카테고리별 사용 가능한 연도 목록
     * GET /api/resources/available-years?category=inspection
     */
    @GetMapping("/available-years")
    public ResponseEntity<List<Integer>> getAvailableYears(@RequestParam String category) {
        List<Integer> years = resourceFileService.getAvailableYears(category);
        return ResponseEntity.ok(years);
    }

    /**
     * 카테고리/연도별 사용 가능한 월 목록
     * GET /api/resources/available-months?category=inspection&year=2026
     */
    @GetMapping("/available-months")
    public ResponseEntity<List<Integer>> getAvailableMonths(
            @RequestParam String category,
            @RequestParam Integer year
    ) {
        List<Integer> months = resourceFileService.getAvailableMonths(category, year);
        return ResponseEntity.ok(months);
    }
}
