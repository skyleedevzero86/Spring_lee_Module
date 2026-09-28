package com.sleekydz86.productrecommend.global.evaluation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SearchMetrics {

	private SearchMetrics() {
	}

	public static double recallAtK(List<String> rankedIds, Set<String> relevantIds, int k) {
		if (relevantIds == null || relevantIds.isEmpty() || k <= 0) {
			return 0.0;
		}
		Set<String> top = new HashSet<>(rankedIds.stream().limit(k).toList());
		long hits = relevantIds.stream().filter(top::contains).count();
		return (double) hits / relevantIds.size();
	}

	public static double mrr(List<String> rankedIds, Set<String> relevantIds) {
		if (relevantIds == null || relevantIds.isEmpty()) {
			return 0.0;
		}
		for (int i = 0; i < rankedIds.size(); i++) {
			if (relevantIds.contains(rankedIds.get(i))) {
				return 1.0 / (i + 1);
			}
		}
		return 0.0;
	}

	public static double precisionAtK(List<String> rankedIds, Set<String> relevantIds, int k) {
		if (k <= 0 || relevantIds == null || relevantIds.isEmpty()) {
			return 0.0;
		}
		long hits = rankedIds.stream().limit(k).filter(relevantIds::contains).count();
		return (double) hits / k;
	}

	public static double ndcgAtK(List<String> rankedIds, Set<String> relevantIds, int k) {
		if (k <= 0 || relevantIds == null || relevantIds.isEmpty()) {
			return 0.0;
		}
		double dcg = 0;
		for (int i = 0; i < Math.min(k, rankedIds.size()); i++) {
			if (relevantIds.contains(rankedIds.get(i))) {
				dcg += 1.0 / (Math.log(i + 2) / Math.log(2));
			}
		}
		int idealHits = Math.min(k, relevantIds.size());
		double idcg = 0;
		for (int i = 0; i < idealHits; i++) {
			idcg += 1.0 / (Math.log(i + 2) / Math.log(2));
		}
		return idcg == 0 ? 0.0 : dcg / idcg;
	}
}
