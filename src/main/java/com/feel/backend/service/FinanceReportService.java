package com.feel.backend.service;

import com.feel.backend.dto.FinanceReportDto;
import com.feel.backend.entity.FinanceReport;
import com.feel.backend.repository.FinanceReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FinanceReportService {

    private final FinanceReportRepository financeReportRepository;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 회계 보고서 목록 조회
     */
    public Page<FinanceReportDto.Response> getReports(
            int page, int size, String keyword, String sortBy, String sortOrder, Integer year
    ) {
        Sort sort = Sort.by(
                "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortBy != null ? sortBy : "createdAt"
        );
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<FinanceReport> reports;

        if (year != null && keyword != null && !keyword.isBlank()) {
            reports = financeReportRepository.searchByYearAndKeyword(year, keyword, pageable);
        } else if (year != null) {
            reports = financeReportRepository.findByYear(year, pageable);
        } else if (keyword != null && !keyword.isBlank()) {
            reports = financeReportRepository.searchByKeyword(keyword, pageable);
        } else {
            reports = financeReportRepository.findAll(pageable);
        }

        return reports.map(this::toResponse);
    }

    /**
     * 회계 보고서 상세 조회
     */
    public FinanceReportDto.Response getReportById(Long id) {
        FinanceReport report = financeReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 회계 보고서를 찾을 수 없습니다."));
        return toResponse(report);
    }

    /**
     * 회계 보고서 전체 개수
     */
    public long getTotalCount() {
        return financeReportRepository.count();
    }

    /**
     * 회계 보고서 등록
     */
    @Transactional
    public FinanceReportDto.Response createReport(FinanceReportDto.Request request) {
        FinanceReport report = FinanceReport.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .fileSize(request.getFileSize())
                .year(request.getYear())
                .month(request.getMonth())
                .build();

        FinanceReport saved = financeReportRepository.save(report);
        return toResponse(saved);
    }

    /**
     * 회계 보고서 수정
     */
    @Transactional
    public FinanceReportDto.Response updateReport(Long id, FinanceReportDto.Request request) {
        FinanceReport report = financeReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 회계 보고서를 찾을 수 없습니다."));

        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setFileName(request.getFileName());
        report.setFileUrl(request.getFileUrl());
        report.setFileSize(request.getFileSize());
        report.setYear(request.getYear());
        report.setMonth(request.getMonth());

        return toResponse(report);
    }

    /**
     * 회계 보고서 삭제
     */
    @Transactional
    public void deleteReport(Long id) {
        FinanceReport report = financeReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("요청한 회계 보고서를 찾을 수 없습니다."));
        financeReportRepository.delete(report);
    }

    /**
     * PDF 파일 업로드
     */
    @Transactional
    public FinanceReportDto.UploadResponse uploadPdf(MultipartFile file) {
        // PDF 파일 검증
        String contentType = file.getContentType();
        if (!"application/pdf".equals(contentType)) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }

        String originalFileName = file.getOriginalFilename();
        String storedFileName = UUID.randomUUID().toString() + ".pdf";

        // 디렉토리 생성
        Path financeDir = Paths.get(uploadDir, "finance");
        try {
            Files.createDirectories(financeDir);
            Path filePath = financeDir.resolve(storedFileName);
            Files.write(filePath, file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }

        String fileUrl = baseUrl + "/uploads/finance/" + storedFileName;

        return FinanceReportDto.UploadResponse.builder()
                .fileUrl(fileUrl)
                .fileName(originalFileName)
                .fileSize(file.getSize())
                .build();
    }

    private FinanceReportDto.Response toResponse(FinanceReport report) {
        return FinanceReportDto.Response.builder()
                .id(report.getId())
                .title(report.getTitle())
                .description(report.getDescription())
                .fileName(report.getFileName())
                .fileUrl(report.getFileUrl())
                .fileSize(report.getFileSize())
                .year(report.getYear())
                .month(report.getMonth())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}
