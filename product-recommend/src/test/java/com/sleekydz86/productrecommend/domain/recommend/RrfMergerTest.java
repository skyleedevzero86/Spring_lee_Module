package com.sleekydz86.productrecommend.domain.recommend;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RrfMergerTest {

	@Test
	void mergesRankedListsWithReciprocalRankFusion() {
		List<RrfMerger.ScoredId> merged = RrfMerger.merge(
			List.of(
				List.of(2L, 1L, 3L),
				List.of(2L, 4L, 1L)
			),
			3
		);
		assertThat(merged).hasSize(3);
		assertThat(merged.getFirst().id()).isEqualTo(2L);
	}
}
