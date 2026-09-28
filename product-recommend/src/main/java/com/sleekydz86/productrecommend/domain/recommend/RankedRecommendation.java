package com.sleekydz86.productrecommend.domain.recommend;

import com.sleekydz86.productrecommend.domain.product.Product;

import java.util.List;

public record RankedRecommendation(
	Product product,
	double score,
	List<String> reasons
) {
	public RankedRecommendation {
		reasons = reasons == null ? List.of() : List.copyOf(reasons);
	}

	public RecommendationEvidence toEvidence() {
		return new RecommendationEvidence(product.id(), score, reasons);
	}
}
