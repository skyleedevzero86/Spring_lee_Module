package com.sleekydz86.catalogflow.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;

public record ProductViewResponse(
		UUID productId,
		String name,
		String summary,
		String description,
		BigDecimal price,
		String currency,
		String status,
		UUID categoryId,
		UUID supplierId,
		String supplierName,
		List<String> imageUrls,
		List<String> keywords,
		List<String> tags,
		boolean aiGenerated,
		String aiModel,
		Instant publishedAt,
		Instant createdAt,
		Instant updatedAt,
		long version) {

	public static ProductViewResponse from(ProductView view) {
		return new ProductViewResponse(
				view.getProductId(),
				view.getName(),
				view.getSummary(),
				view.getDescription(),
				view.getPrice(),
				view.getCurrency(),
				view.getStatus(),
				view.getCategoryId(),
				view.getSupplierId(),
				view.getSupplierName(),
				view.getImageUrls(),
				view.getKeywords(),
				view.getTags(),
				view.isAiGenerated(),
				view.getAiModel(),
				view.getPublishedAt(),
				view.getCreatedAt(),
				view.getUpdatedAt(),
				view.getVersion());
	}
}
