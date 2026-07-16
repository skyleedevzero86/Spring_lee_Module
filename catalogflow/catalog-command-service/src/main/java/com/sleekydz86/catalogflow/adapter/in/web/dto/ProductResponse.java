package com.sleekydz86.catalogflow.adapter.in.web.dto;

import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.domain.model.AiEnrichmentStatus;
import com.sleekydz86.catalogflow.domain.model.ProductStatus;

public record ProductResponse(
		UUID productId,
		String name,
		ProductStatus status,
		AiEnrichmentStatus aiEnrichmentStatus,
		long version,
		Instant createdAt,
		Instant updatedAt) {

	public static ProductResponse from(ProductCommandResult result) {
		return new ProductResponse(
				result.productId(),
				result.name(),
				result.status(),
				result.aiEnrichmentStatus(),
				result.version(),
				result.createdAt(),
				result.updatedAt());
	}
}
