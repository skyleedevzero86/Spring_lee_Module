package com.sleekydz86.storagemigration.file.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "파일 정보 삭제 요청")
public record DeleteFilesRequest(
        @Schema(description = "지울 파일 ID 목록") @NotEmpty List<String> ids,
        @Schema(description = "true이면 스토리지 파일도 함께 지웁니다", example = "false") boolean deleteFromStorage) {
}
