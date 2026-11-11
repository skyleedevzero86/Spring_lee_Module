package com.sleekydz86.storagemigration.audit.adapter.in.web;

import com.sleekydz86.storagemigration.audit.application.port.in.AuditLogQueryUseCase;
import com.sleekydz86.storagemigration.audit.domain.AuditLog;
import com.sleekydz86.storagemigration.audit.domain.AuditSearchCondition;
import com.sleekydz86.storagemigration.global.response.ApiResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import com.sleekydz86.storagemigration.global.error.ErrorCode;
import com.sleekydz86.storagemigration.global.openapi.ApiErrorCodes;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "작업 로그")
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogQueryUseCase query;

    public AuditLogController(AuditLogQueryUseCase query) {
        this.query = query;
    }

    @Operation(summary = "작업 로그 조회 (작업자·IP·일시)",
            description = "SM_AUDIT_LOG를 최근 작업부터 조회합니다. 조건을 비우면 전체에서 limit건을 돌려줍니다. 사용 중인 DB가 있어야 합니다.")
    @ApiErrorCodes({ErrorCode.DB_NOT_CONNECTED, ErrorCode.DB_OPERATION_FAILED})
    @GetMapping
    public ApiResponse<List<AuditLog>> search(
            @Parameter(description = "대상 종류 (STORED_FILE, STORAGE_OBJECT, STORAGE_SERVER, DB_CONNECTION)", example = "STORED_FILE") @RequestParam(required = false) String targetType,
            @Parameter(description = "대상 ID (파일 ID, 스토리지 서버 ID 등)") @RequestParam(required = false) String targetId,
            @Parameter(description = "작업자 이름 (X-Worker로 기록된 값)", example = "kim") @RequestParam(required = false) String worker,
            @Parameter(description = "최대 건수", example = "200") @RequestParam(defaultValue = "200") int limit) {
        return ApiResponse.ok(query.search(new AuditSearchCondition(targetType, targetId, worker, limit)));
    }
}
