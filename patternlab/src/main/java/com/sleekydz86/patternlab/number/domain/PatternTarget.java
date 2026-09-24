package com.sleekydz86.patternlab.number.domain;

public record PatternTarget(String typeName, String category) {

	public PatternTarget {
		if (typeName == null || typeName.isBlank()) {
			throw new IllegalArgumentException("대상 타입 이름이 필요합니다");
		}
		if (category == null || category.isBlank()) {
			throw new IllegalArgumentException("분류 이름이 필요합니다");
		}
	}
}
