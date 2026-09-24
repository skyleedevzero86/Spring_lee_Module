package com.sleekydz86.patternlab.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sleekydz86.patternlab.global.pattern.SwitchPrimitivePatternAnalyzer;
import com.sleekydz86.patternlab.number.application.port.in.AnalyzeNumberUseCase;
import com.sleekydz86.patternlab.number.application.port.out.NumberParserPort;
import com.sleekydz86.patternlab.number.application.port.out.PrimitivePatternAnalyzerPort;
import com.sleekydz86.patternlab.number.application.service.AnalyzeNumberService;

@Configuration
public class ApplicationConfiguration {

	@Bean
	PrimitivePatternAnalyzerPort primitivePatternAnalyzerPort() {
		return new SwitchPrimitivePatternAnalyzer();
	}

	@Bean
	AnalyzeNumberUseCase analyzeNumberUseCase(
		NumberParserPort numberParserPort,
		PrimitivePatternAnalyzerPort primitivePatternAnalyzerPort
	) {
		return new AnalyzeNumberService(numberParserPort, primitivePatternAnalyzerPort);
	}
}
