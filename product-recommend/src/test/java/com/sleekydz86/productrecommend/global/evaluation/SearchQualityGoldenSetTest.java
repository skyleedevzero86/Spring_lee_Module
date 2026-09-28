package com.sleekydz86.productrecommend.global.evaluation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SearchQualityGoldenSetTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void evaluatesGoldenSetRecallAndMrr() throws Exception {
		List<GoldenCase> cases;
		try (InputStream in = getClass().getResourceAsStream("/golden/search-golden-set.json")) {
			cases = mapper.readValue(in, new TypeReference<>() {
			});
		}

		Map<String, String> catalog = new HashMap<>();
		catalog.put("p1", "노이즈캔슬링 이어폰");
		catalog.put("p2", "블루투스 이어버드");
		catalog.put("p3", "MacBook Pro 16 inch");
		catalog.put("p4", "맥북 프로 16");
		catalog.put("p5", "에티오피아 원두");
		catalog.put("p6", "스페셜티 블렌드");
		catalog.put("p7", "게이밍 마우스");

		double recallSum = 0;
		double mrrSum = 0;
		for (GoldenCase golden : cases) {
			List<String> ranked = rankByTokenOverlap(golden.query(), catalog);
			Set<String> relevantIds = new HashSet<>();
			catalog.forEach((id, name) -> {
				if (golden.relevantProductNames().contains(name)) {
					relevantIds.add(id);
				}
			});
			recallSum += SearchMetrics.recallAtK(ranked, relevantIds, 3);
			mrrSum += SearchMetrics.mrr(ranked, relevantIds);
		}

		double avgRecall = recallSum / cases.size();
		double avgMrr = mrrSum / cases.size();
		assertThat(avgRecall).as("Recall@3").isGreaterThan(0.5);
		assertThat(avgMrr).as("MRR").isGreaterThan(0.5);
	}

	private static List<String> rankByTokenOverlap(String query, Map<String, String> catalog) {
		String q = query.toLowerCase();
		return catalog.entrySet().stream()
			.sorted((a, b) -> Integer.compare(overlap(q, b.getValue()), overlap(q, a.getValue())))
			.map(Map.Entry::getKey)
			.toList();
	}

	private static int overlap(String query, String name) {
		int score = 0;
		for (String token : name.toLowerCase().split("\\s+")) {
			if (query.contains(token) || token.contains(query.replace(" ", ""))) {
				score += token.length();
			}
		}
		return score;
	}

	record GoldenCase(String query, List<String> relevantProductNames) {
	}
}
