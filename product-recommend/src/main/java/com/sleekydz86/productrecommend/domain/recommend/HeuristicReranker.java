package com.sleekydz86.productrecommend.domain.recommend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HeuristicReranker {

	private HeuristicReranker() {
	}

	public static List<RankedRecommendation> rerank(List<RankedRecommendation> candidates, int topK) {
		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}
		List<RankedRecommendation> copy = new ArrayList<>(candidates);
		copy.sort(Comparator
			.comparingDouble(RankedRecommendation::score).reversed()
			.thenComparing(r -> r.product().popularityScore(), Comparator.reverseOrder())
			.thenComparing(r -> r.reasons().size(), Comparator.reverseOrder()));
		return copy.stream().limit(Math.max(1, topK)).toList();
	}
}
