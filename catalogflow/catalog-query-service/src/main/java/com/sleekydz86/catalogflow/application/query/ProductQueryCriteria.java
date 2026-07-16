package com.sleekydz86.catalogflow.application.query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductQueryCriteria(
		String status,
		UUID categoryId,
		String name,
		String keyword,
		BigDecimal minPrice,
		BigDecimal maxPrice,
		Instant cursorPublishedAt,
		String cursorProductId,
		int size) {

	public ProductQueryCriteria {
		if (size < 1) {
			size = 20;
		}
		if (size > 100) {
			size = 100;
		}
	}

	public static ProductQueryCriteria list(String status, Instant cursorPublishedAt, String cursorProductId, int size) {
		return new ProductQueryCriteria(status, null, null, null, null, null, cursorPublishedAt, cursorProductId, size);
	}

	public static ProductQueryCriteria category(
			UUID categoryId,
			String status,
			Instant cursorPublishedAt,
			String cursorProductId,
			int size) {
		return new ProductQueryCriteria(
				status,
				categoryId,
				null,
				null,
				null,
				null,
				cursorPublishedAt,
				cursorProductId,
				size);
	}

	public static ProductQueryCriteria search(
			String name,
			String keyword,
			String status,
			BigDecimal minPrice,
			BigDecimal maxPrice,
			Instant cursorPublishedAt,
			String cursorProductId,
			int size) {
		return new ProductQueryCriteria(
				status,
				null,
				name,
				keyword,
				minPrice,
				maxPrice,
				cursorPublishedAt,
				cursorProductId,
				size);
	}

	public static ProductQueryCriteria popular(int size) {
		return new ProductQueryCriteria("PUBLISHED", null, null, null, null, null, null, null, size);
	}
}
