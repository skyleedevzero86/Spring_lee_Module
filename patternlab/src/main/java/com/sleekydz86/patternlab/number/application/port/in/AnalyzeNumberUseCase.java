package com.sleekydz86.patternlab.number.application.port.in;

import com.sleekydz86.patternlab.number.domain.NumberAnalysis;

public interface AnalyzeNumberUseCase {

	NumberAnalysis analyze(String rawValue);
}
