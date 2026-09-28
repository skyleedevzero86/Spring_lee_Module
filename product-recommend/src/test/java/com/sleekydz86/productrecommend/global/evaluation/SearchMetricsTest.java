package com.sleekydz86.productrecommend.global.evaluation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class SearchMetricsTest {

	@Test
	void recallAtKAndMrr() {
		List<String> ranked = List.of("a", "b", "c", "d");
		Set<String> relevant = Set.of("c", "x");

		assertThat(SearchMetrics.recallAtK(ranked, relevant, 3)).isCloseTo(0.5, within(1e-9));
		assertThat(SearchMetrics.mrr(ranked, relevant)).isCloseTo(1.0 / 3.0, within(1e-9));
	}
}
