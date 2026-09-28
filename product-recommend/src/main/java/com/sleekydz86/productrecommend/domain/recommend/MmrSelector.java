package com.sleekydz86.productrecommend.domain.recommend;

import com.sleekydz86.productrecommend.domain.product.Product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public final class MmrSelector {

	private MmrSelector() {
	}

	public static List<RankedRecommendation> select(List<RankedRecommendation> candidates, int topK, double lambda) {
		if (candidates == null || candidates.isEmpty()) {
			return List.of();
		}
		double lam = Math.max(0, Math.min(1, lambda <= 0 ? 0.7 : lambda));
		int limit = Math.max(1, Math.min(topK, candidates.size()));
		List<RankedRecommendation> remaining = new ArrayList<>(candidates);
		List<RankedRecommendation> selected = new ArrayList<>();
		Set<String> brands = new HashSet<>();
		Set<String> categories = new HashSet<>();

		while (selected.size() < limit && !remaining.isEmpty()) {
			RankedRecommendation best = null;
			double bestScore = Double.NEGATIVE_INFINITY;
			for (RankedRecommendation candidate : remaining) {
				double relevance = candidate.score();
				double redundancy = redundancy(candidate.product(), brands, categories);
				double mmr = lam * relevance - (1 - lam) * redundancy;
				if (mmr > bestScore) {
					bestScore = mmr;
					best = candidate;
				}
			}
			if (best == null) {
				break;
			}
			selected.add(best);
			remaining.remove(best);
			if (best.product().brand() != null) {
				brands.add(norm(best.product().brand()));
			}
			if (best.product().category() != null) {
				categories.add(norm(best.product().category()));
			}
		}
		return selected;
	}

	private static double redundancy(Product product, Set<String> brands, Set<String> categories) {
		double score = 0;
		if (product.brand() != null && brands.contains(norm(product.brand()))) {
			score += 0.7;
		}
		if (product.category() != null && categories.contains(norm(product.category()))) {
			score += 0.3;
		}
		return Math.min(1.0, score);
	}

	private static String norm(String value) {
		return value.trim().toUpperCase(Locale.ROOT);
	}
}
