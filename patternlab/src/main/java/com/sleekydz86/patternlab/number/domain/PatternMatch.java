package com.sleekydz86.patternlab.number.domain;

public record PatternMatch(PatternTarget target, boolean matched) {

	public PatternMatch {
		if (target == null) {
			throw new IllegalArgumentException("패턴 대상이 필요합니다");
		}
	}
}
