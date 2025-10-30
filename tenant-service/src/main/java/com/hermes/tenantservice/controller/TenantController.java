package com.hermes.tenantservice.controller;

import com.hermes.tenantservice.dto.CreateTenantRequest;
import com.hermes.tenantservice.dto.TenantResponse;
import com.hermes.tenantservice.dto.UpdateTenantRequest;
import com.hermes.tenantservice.dto.PagedResponse;
import com.hermes.tenantservice.service.TenantManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 테넌트 관리 REST API 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/tenants")
@RequiredArgsConstructor
@Tag(name = "Tenant Management", description = "테넌트 관리 API")
public class TenantController {

    private final TenantManagementService tenantManagementService;

    /**
     * 테넌트 생성
     */
    @PostMapping
    @Operation(summary = "테넌트 생성", description = "새로운 테넌트를 생성하고 필요시 스키마를 초기화합니다")
    @ApiResponse(responseCode = "201", description = "테넌트 생성 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    @ApiResponse(responseCode = "409", description = "테넌트 ID 중복")
    public ResponseEntity<TenantResponse> createTenant(
            @Valid @RequestBody CreateTenantRequest request) {

        log.info("테넌트 생성 요청: tenantId={}, name={}", request.getTenantId(), request.getName());

        TenantResponse response = tenantManagementService.createTenant(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * 테넌트 목록 조회 (페이징)
     */
    @GetMapping
    @Operation(summary = "테넌트 목록 조회", description = "페이징된 테넌트 목록을 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    public ResponseEntity<PagedResponse<TenantResponse>> getTenants(
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "정렬 기준 (예: createdAt,desc)")
            @RequestParam(defaultValue = "createdAt") String sort) {

        log.debug("테넌트 목록 조회 요청: page={}, size={}, sort={}", page, size, sort);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sort));
        PagedResponse<TenantResponse> response = tenantManagementService.getTenants(pageRequest);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 테넌트 조회
     */
    @GetMapping("/{tenantId}")
    @Operation(summary = "테넌트 상세 조회", description = "특정 테넌트의 상세 정보를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "테넌트를 찾을 수 없음")
    public ResponseEntity<TenantResponse> getTenant(
            @Parameter(description = "테넌트 ID")
            @PathVariable String tenantId) {

        log.debug("테넌트 조회 요청: tenantId={}", tenantId);

        TenantResponse response = tenantManagementService.getTenant(tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * 테넌트 정보 수정
     */
    @PutMapping("/{tenantId}")
    @Operation(summary = "테넌트 정보 수정", description = "테넌트의 정보를 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "테넌트를 찾을 수 없음")
    public ResponseEntity<TenantResponse> updateTenant(
            @Parameter(description = "테넌트 ID")
            @PathVariable String tenantId,
            @Valid @RequestBody UpdateTenantRequest request) {

        log.info("테넌트 수정 요청: tenantId={}", tenantId);

        TenantResponse response = tenantManagementService.updateTenant(tenantId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 테넌트 활성화/비활성화
     */
    @PatchMapping("/{tenantId}/status")
    @Operation(summary = "테넌트 상태 변경", description = "테넌트의 상태를 변경합니다")
    @ApiResponse(responseCode = "200", description = "상태 변경 성공")
    @ApiResponse(responseCode = "404", description = "테넌트를 찾을 수 없음")
    public ResponseEntity<TenantResponse> updateTenantStatus(
            @Parameter(description = "테넌트 ID")
            @PathVariable String tenantId,
            @Parameter(description = "변경할 상태", example = "ACTIVE")
            @RequestParam String status) {

        log.info("테넌트 상태 변경 요청: tenantId={}, status={}", tenantId, status);

        TenantResponse response = tenantManagementService.updateTenantStatus(tenantId, status);
        return ResponseEntity.ok(response);
    }
}
