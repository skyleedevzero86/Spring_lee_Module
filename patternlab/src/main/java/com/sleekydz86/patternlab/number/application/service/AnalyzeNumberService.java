package com.sleekydz86.patternlab.number.application.service;

import com.sleekydz86.patternlab.number.application.port.in.AnalyzeNumberUseCase;
import com.sleekydz86.patternlab.number.application.port.out.NumberParserPort;
import com.sleekydz86.patternlab.number.application.port.out.PrimitivePatternAnalyzerPort;
import com.sleekydz86.patternlab.number.domain.NumberAnalysis;
import com.sleekydz86.patternlab.number.domain.ParsedNumber;

public final class AnalyzeNumberService implements AnalyzeNumberUseCase {

	private final NumberParserPort numberParserPort;
	private final PrimitivePatternAnalyzerPort primitivePatternAnalyzerPort;

	public AnalyzeNumberService(
		NumberParserPort numberParserPort,
		PrimitivePatternAnalyzerPort primitivePatternAnalyzerPort
	) {
		this.numberParserPort = numberParserPort;
		this.primitivePatternAnalyzerPort = primitivePatternAnalyzerPort;
	}

	@Override
	public NumberAnalysis analyze(String rawValue) {
		ParsedNumber parsed = numberParserPort.parse(rawValue);
		return primitivePatternAnalyzerPort.analyze(rawValue.trim(), parsed);
	}
}
