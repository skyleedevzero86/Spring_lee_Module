package com.sleekydz86.storagemigration.audit.adapter.in.web;

import com.sleekydz86.storagemigration.audit.application.port.in.AuditLogQueryUseCase;
import com.sleekydz86.storagemigration.audit.domain.AuditLog;
import com.sleekydz86.storagemigration.audit.domain.AuditSearchCondition;
import com.sleekydz86.storagemigration.global.response.ApiResponse;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "작업 로그 조회 (작업자·IP·일시)")
    @GetMapping
    public ApiResponse<List<AuditLog>> search(@RequestParam(required = false) String targetType,
                                              @RequestParam(required = false) String targetId,
                                              @RequestParam(required = false) String worker,
                                              @RequestParam(defaultValue = "200") int limit) {
        return ApiResponse.ok(query.search(new AuditSearchCondition(targetType, targetId, worker, limit)));
    }
}
