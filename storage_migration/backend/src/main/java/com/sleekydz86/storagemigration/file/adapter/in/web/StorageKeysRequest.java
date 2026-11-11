package com.sleekydz86.storagemigration.file.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "스토리지 파일 등록·삭제 요청")
public record StorageKeysRequest(
        @Schema(description = "스토리지 서버 ID") @NotBlank String storageServerId,
        @Schema(description = "스토리지 객체 키 목록", example = "[\"uploads/2026/11/report.pdf\"]") @NotEmpty List<String> keys) {
}
