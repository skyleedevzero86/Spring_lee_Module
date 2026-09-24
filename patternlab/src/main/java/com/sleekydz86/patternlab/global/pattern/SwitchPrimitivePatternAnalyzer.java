package com.sleekydz86.patternlab.global.pattern;

import java.util.List;

import com.sleekydz86.patternlab.number.application.port.out.PrimitivePatternAnalyzerPort;
import com.sleekydz86.patternlab.number.domain.NumberAnalysis;
import com.sleekydz86.patternlab.number.domain.ParsedNumber;
import com.sleekydz86.patternlab.number.domain.PatternMatch;
import com.sleekydz86.patternlab.number.domain.PatternTarget;

public final class SwitchPrimitivePatternAnalyzer implements PrimitivePatternAnalyzerPort {

	@Override
	public NumberAnalysis analyze(String rawValue, ParsedNumber parsedNumber) {
		Object value = parsedNumber.boxedValue();
		String primaryCategory = classify(value);
		List<PatternMatch> matches = List.of(
			new PatternMatch(new PatternTarget("Integer", "정수"), value instanceof int),
			new PatternMatch(new PatternTarget("Long", "큰 정수"), value instanceof long),
			new PatternMatch(new PatternTarget("Float", "실수"), value instanceof float),
			new PatternMatch(new PatternTarget("Double", "실수"), value instanceof double)
		);
		return new NumberAnalysis(rawValue, parsedNumber.detectedType(), primaryCategory, matches);
	}

	private static String classify(Object value) {
		return switch (value) {
			case int i -> "정수";
			case long l -> "큰 정수";
			case float f -> "실수";
			case double d -> "실수";
			default -> "알 수 없음";
		};
	}
}
