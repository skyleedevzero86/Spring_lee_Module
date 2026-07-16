package com.sleekydz86.catalogflow.application.command;

import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.domain.model.AiEnrichmentStatus;
import com.sleekydz86.catalogflow.domain.model.ProductStatus;

public record ProductCommandResult(
		UUID productId,
		String name,
		ProductStatus status,
		AiEnrichmentStatus aiEnrichmentStatus,
		long version,
		Instant createdAt,
		Instant updatedAt) {
}
