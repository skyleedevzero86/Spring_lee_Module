package com.sleekydz86.catalogflow.application.port.out;

import java.util.List;
import java.util.UUID;

public interface AiEnrichmentPort {

	EnrichmentResult enrich(EnrichmentRequest request);

	record EnrichmentRequest(
			UUID productId,
			String name,
			String description,
			UUID categoryId,
			String supplierName) {
	}

	record EnrichmentResult(
			String modelName,
			String summary,
			String generatedDescription,
			List<String> keywords,
			List<String> tags,
			String recommendedCategory,
			String warnings,
			boolean requiresHumanReview,
			double confidence,
			String promptVersion) {
	}
}
