package com.sleekydz86.catalogflow.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterProductImageRequest(
		@NotNull Long version,
		@NotBlank @Size(max = 500) String storageKey,
		@NotBlank @Size(max = 100) String contentType,
		@Min(1) long sizeInBytes,
		boolean temporary) {
}
