package com.sleekydz86.patternlab.number.domain;

import java.util.List;

public record NumberAnalysis(
	String rawValue,
	DetectedType detectedType,
	String primaryCategory,
	List<PatternMatch> matches
) {

	public NumberAnalysis {
		if (rawValue == null || rawValue.isBlank()) {
			throw new IllegalArgumentException("입력 값이 필요합니다");
		}
		if (detectedType == null) {
			throw new IllegalArgumentException("감지된 타입이 필요합니다");
		}
		if (primaryCategory == null || primaryCategory.isBlank()) {
			throw new IllegalArgumentException("기본 분류가 필요합니다");
		}
		if (matches == null || matches.isEmpty()) {
			throw new IllegalArgumentException("패턴 매칭 결과가 필요합니다");
		}
		matches = List.copyOf(matches);
	}
}
