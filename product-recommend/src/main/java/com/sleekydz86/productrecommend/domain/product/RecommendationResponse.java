package com.sleekydz86.productrecommend.domain.product;

import java.util.List;

public record RecommendationResponse(
	List<Long> productIds,
	String explanation
) {
}
