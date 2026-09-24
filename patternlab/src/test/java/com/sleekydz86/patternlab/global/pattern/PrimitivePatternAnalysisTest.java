package com.sleekydz86.patternlab.global.pattern;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.sleekydz86.patternlab.number.adapter.out.parsing.TextNumberParserAdapter;
import com.sleekydz86.patternlab.number.application.service.AnalyzeNumberService;
import com.sleekydz86.patternlab.number.domain.DetectedType;
import com.sleekydz86.patternlab.number.domain.NumberAnalysis;

class PrimitivePatternAnalysisTest {

	private final AnalyzeNumberService service =
		new AnalyzeNumberService(new TextNumberParserAdapter(), new SwitchPrimitivePatternAnalyzer());

	@Test
	void analyzesDoubleWithPrimitivePatterns() {
		NumberAnalysis analysis = service.analyze("125.45");

		assertThat(analysis.detectedType()).isEqualTo(DetectedType.DOUBLE);
		assertThat(analysis.primaryCategory()).isEqualTo("실수");
		assertThat(find(analysis, "Integer").matched()).isFalse();
		assertThat(find(analysis, "Long").matched()).isFalse();
		assertThat(find(analysis, "Double").matched()).isTrue();
		assertThat(find(analysis, "Double").target().category()).isEqualTo("실수");
	}

	@Test
	void analyzesIntegerExactMatches() {
		NumberAnalysis analysis = service.analyze("125");

		assertThat(analysis.detectedType()).isEqualTo(DetectedType.INTEGER);
		assertThat(analysis.primaryCategory()).isEqualTo("정수");
		assertThat(find(analysis, "Integer").matched()).isTrue();
		assertThat(find(analysis, "Long").matched()).isTrue();
	}

	private static com.sleekydz86.patternlab.number.domain.PatternMatch find(
		NumberAnalysis analysis,
		String typeName
	) {
		return analysis.matches().stream()
			.filter(match -> match.target().typeName().equals(typeName))
			.findFirst()
			.orElseThrow();
	}
}
