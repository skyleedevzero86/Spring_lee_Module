package com.sleekydz86.concurrentlab.customer.domain;

import java.util.List;

public record Recommendation(String productId, String title, int score) {

	public Recommendation {
		if (productId == null || productId.isBlank()) {
			throw new IllegalArgumentException("추천 상품 번호가 필요합니다");
		}
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("추천 상품명이 필요합니다");
		}
		if (score < 0 || score > 100) {
			throw new IllegalArgumentException("추천 점수는 0에서 100 사이여야 합니다");
		}
	}

	public static List<Recommendation> sampleFor(CustomerId customerId) {
		return List.of(
			new Recommendation("REC-" + customerId.value() + "-A", "스마트 워치", 92),
			new Recommendation("REC-" + customerId.value() + "-B", "블루투스 스피커", 87)
		);
	}
}
