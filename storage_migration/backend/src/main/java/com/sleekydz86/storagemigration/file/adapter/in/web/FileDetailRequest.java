package com.sleekydz86.storagemigration.file.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "파일 상세 정보 수정·DB 저장 요청")
public record FileDetailRequest(
        @Schema(description = "제목 (200자까지)", example = "11월 심의 보고서") @Size(max = 200) String title,
        @Schema(description = "설명 (2000자까지)", example = "1차 이관 리허설에 쓴 파일") @Size(max = 2000) String description,
        @Schema(description = "태그 목록", example = "[\"심의\", \"2026\"]") List<String> tags) {
}
