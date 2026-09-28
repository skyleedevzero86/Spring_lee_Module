package com.sleekydz86.productrecommend.domain.recommend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RrfMerger {

	private static final int DEFAULT_K = 60;

	private RrfMerger() {
	}

	public static List<ScoredId> merge(List<List<Long>> rankedLists, int topK) {
		Map<Long, Double> scores = new HashMap<>();
		for (List<Long> list : rankedLists) {
			for (int rank = 0; rank < list.size(); rank++) {
				Long id = list.get(rank);
				scores.merge(id, 1.0 / (DEFAULT_K + rank + 1), Double::sum);
			}
		}
		List<ScoredId> merged = new ArrayList<>();
		scores.forEach((id, score) -> merged.add(new ScoredId(id, score)));
		merged.sort(Comparator.comparingDouble(ScoredId::score).reversed());
		return merged.stream().limit(Math.max(1, topK)).toList();
	}

	public record ScoredId(Long id, double score) {
	}
}
