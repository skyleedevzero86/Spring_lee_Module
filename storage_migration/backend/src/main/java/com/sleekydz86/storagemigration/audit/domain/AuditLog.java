package com.sleekydz86.storagemigration.audit.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "작업 로그 (SM_AUDIT_LOG)")
public record AuditLog(
        @Schema(description = "로그 번호") long id,
        @Schema(description = "작업 (예시 STORED_FILE_C, STORAGE_SERVER_U, STORED_FILE_SAVE)") String action,
        @Schema(description = "대상 종류 (STORED_FILE, STORAGE_OBJECT, STORAGE_SERVER, DB_CONNECTION)") String targetType,
        @Schema(description = "대상 ID") String targetId,
        @Schema(description = "상세 내용") String detail,
        @Schema(description = "작업자") String worker,
        @Schema(description = "작업 IP") String workIp,
        @Schema(description = "작업 일시") Instant workedAt) {
}
