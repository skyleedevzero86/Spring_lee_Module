package com.sleekydz86.catalogflow.adapter.in.web.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateProductRequest(
		@NotNull Long version,
		@NotBlank @Size(max = 200) String name,
		@Size(max = 10000) String description,
		@NotNull UUID categoryId,
		@NotNull UUID supplierId) {
}
