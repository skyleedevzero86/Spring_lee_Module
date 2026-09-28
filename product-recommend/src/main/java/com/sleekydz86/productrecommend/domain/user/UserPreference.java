package com.sleekydz86.productrecommend.domain.user;

import java.math.BigDecimal;
import java.time.Instant;

public record UserPreference(
	String userId,
	String preferredCategory,
	String preferredBrand,
	BigDecimal minPrice,
	BigDecimal maxPrice,
	float[] embedding,
	Instant updatedAt
) {
	public static UserPreference empty(String userId) {
		return new UserPreference(userId, null, null, null, null, null, Instant.now());
	}

	public UserPreference withCategory(String category) {
		return new UserPreference(userId, category, preferredBrand, minPrice, maxPrice, embedding, Instant.now());
	}

	public UserPreference withBrand(String brand) {
		return new UserPreference(userId, preferredCategory, brand, minPrice, maxPrice, embedding, Instant.now());
	}

	public UserPreference withPriceRange(BigDecimal min, BigDecimal max) {
		return new UserPreference(userId, preferredCategory, preferredBrand, min, max, embedding, Instant.now());
	}

	public UserPreference withEmbedding(float[] vector) {
		return new UserPreference(
			userId,
			preferredCategory,
			preferredBrand,
			minPrice,
			maxPrice,
			vector == null ? null : vector.clone(),
			Instant.now()
		);
	}
}
