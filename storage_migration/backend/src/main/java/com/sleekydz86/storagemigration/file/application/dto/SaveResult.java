package com.sleekydz86.storagemigration.file.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "DB 저장 결과")
public record SaveResult(
        @Schema(description = "저장 뒤 파일 정보") StoredFileResult file,
        @Schema(description = "스토리지 원본 크기 (바이트)") long storedBytes,
        @Schema(description = "DB CONTENT에 들어간 크기 (바이트)") long databaseBytes,
        @Schema(description = "저장하면서 계산한 SHA-256") String checksum,
        @Schema(description = "크기와 체크섬이 모두 맞으면 true") boolean verified) {
}
