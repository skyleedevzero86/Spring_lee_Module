package com.sleekydz86.catalogflow.application.service;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.domain.model.Product;

final class ProductCommandResultMapper {

	private ProductCommandResultMapper() {
	}

	static ProductCommandResult toResult(Product product) {
		return new ProductCommandResult(
				product.getId().value(),
				product.getName().value(),
				product.getStatus(),
				product.getAiEnrichmentStatus(),
				product.getVersion(),
				product.getCreatedAt(),
				product.getUpdatedAt());
	}
}
