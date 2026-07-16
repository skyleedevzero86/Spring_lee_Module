package com.sleekydz86.catalogflow.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LaunchCsvImportRequest(
		@NotBlank(message = "CSV 파일 경로는 필수입니다") String filePath) {
}
