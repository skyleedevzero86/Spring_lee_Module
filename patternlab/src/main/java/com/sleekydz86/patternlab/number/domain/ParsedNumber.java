package com.sleekydz86.patternlab.number.domain;

public record ParsedNumber(DetectedType detectedType, Object boxedValue) {

	public ParsedNumber {
		if (detectedType == null) {
			throw new IllegalArgumentException("감지된 타입이 필요합니다");
		}
		if (boxedValue == null) {
			throw new IllegalArgumentException("박스 값이 필요합니다");
		}
	}
}
