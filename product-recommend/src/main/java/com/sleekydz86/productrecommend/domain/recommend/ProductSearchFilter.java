package com.sleekydz86.productrecommend.domain.recommend;

import java.math.BigDecimal;

public record ProductSearchFilter(
	String category,
	String brand,
	String excludeBrand,
	String color,
	BigDecimal minPrice,
	BigDecimal maxPrice,
	boolean availableOnly
) {
}
