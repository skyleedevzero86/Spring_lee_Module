package com.sleekydz86.patternlab.number.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.patternlab.number.adapter.in.web.NumberWebModels.AnalyzeRequest;
import com.sleekydz86.patternlab.number.adapter.in.web.NumberWebModels.AnalyzeResponse;
import com.sleekydz86.patternlab.number.application.port.in.AnalyzeNumberUseCase;

@RestController
@RequestMapping("/api/patterns")
public class NumberPatternController {

	private final AnalyzeNumberUseCase analyzeNumberUseCase;

	public NumberPatternController(AnalyzeNumberUseCase analyzeNumberUseCase) {
		this.analyzeNumberUseCase = analyzeNumberUseCase;
	}

	@PostMapping("/analyze")
	public AnalyzeResponse analyze(@RequestBody AnalyzeRequest request) {
		if (request == null || request.value() == null) {
			throw new IllegalArgumentException("입력 값이 필요합니다");
		}
		return AnalyzeResponse.from(analyzeNumberUseCase.analyze(request.value()));
	}
}
