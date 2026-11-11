package com.sleekydz86.storagemigration.file.application.dto;

import com.sleekydz86.storagemigration.file.domain.FileStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

@Schema(description = "스토리지에 있는 파일")
public record StorageFileView(
        @Schema(description = "스토리지 객체 키") String key,
        @Schema(description = "파일 이름") String name,
        @Schema(description = "파일 크기 (바이트)") long size,
        @Schema(description = "스토리지에서 마지막으로 바뀐 일시") Instant lastModified,
        @Schema(description = "DB에 등록된 파일 ID. 등록되지 않았으면 null") String storedFileId,
        @Schema(description = "DB 등록 상태. 등록되지 않았으면 null") FileStatus status) {
}
