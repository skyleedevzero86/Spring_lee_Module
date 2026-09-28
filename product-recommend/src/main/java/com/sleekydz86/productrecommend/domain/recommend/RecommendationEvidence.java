package com.sleekydz86.productrecommend.domain.recommend;

import java.util.List;

public record RecommendationEvidence(
	Long productId,
	double score,
	List<String> reasons
) {
	public RecommendationEvidence {
		reasons = reasons == null ? List.of() : List.copyOf(reasons);
	}
}
