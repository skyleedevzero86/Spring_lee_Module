package com.sleekydz86.pemlab.pem.adapter.in.web;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.pemlab.pem.adapter.in.web.PemWebModels.AnalyzeRequest;
import com.sleekydz86.pemlab.pem.adapter.in.web.PemWebModels.GenerateResponse;
import com.sleekydz86.pemlab.pem.adapter.in.web.PemWebModels.InspectionResponse;
import com.sleekydz86.pemlab.pem.application.port.in.AnalyzePemUseCase;
import com.sleekydz86.pemlab.pem.application.port.in.GenerateKeyPairUseCase;

@RestController
@RequestMapping("/api/pem")
public class PemController {

	private final AnalyzePemUseCase analyzePemUseCase;
	private final GenerateKeyPairUseCase generateKeyPairUseCase;

	public PemController(
		AnalyzePemUseCase analyzePemUseCase,
		GenerateKeyPairUseCase generateKeyPairUseCase
	) {
		this.analyzePemUseCase = analyzePemUseCase;
		this.generateKeyPairUseCase = generateKeyPairUseCase;
	}

	@PostMapping("/analyze")
	public InspectionResponse analyze(@RequestBody AnalyzeRequest request) {
		return InspectionResponse.from(analyzePemUseCase.analyze(request.toDomain()));
	}

	@PostMapping("/generate")
	public GenerateResponse generate() {
		return GenerateResponse.from(generateKeyPairUseCase.generate());
	}
}
