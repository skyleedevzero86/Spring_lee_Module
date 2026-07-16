package com.sleekydz86.catalogflow.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePresignedUploadUrlRequest(
		@NotBlank @Size(max = 100) String contentType,
		@Min(1) long sizeInBytes,
		@NotBlank @Size(max = 255) String fileName,
		boolean temporary) {
}
