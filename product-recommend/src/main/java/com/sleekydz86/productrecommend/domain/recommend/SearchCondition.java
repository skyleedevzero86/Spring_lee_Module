package com.sleekydz86.productrecommend.domain.recommend;

import java.math.BigDecimal;
import java.util.List;

public record SearchCondition(
	String semanticQuery,
	String category,
	String brand,
	String excludeBrand,
	String color,
	BigDecimal minPrice,
	BigDecimal maxPrice,
	List<String> attributes,
	int limit
) {
	public SearchCondition {
		attributes = attributes == null ? List.of() : List.copyOf(attributes);
		limit = Math.max(1, Math.min(limit <= 0 ? 20 : limit, 100));
	}

	public static SearchCondition ofQuery(String query, int limit) {
		return new SearchCondition(query, null, null, null, null, null, null, List.of(), limit);
	}
}
