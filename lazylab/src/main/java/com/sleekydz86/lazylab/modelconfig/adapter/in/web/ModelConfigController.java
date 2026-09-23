package com.sleekydz86.lazylab.modelconfig.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sleekydz86.lazylab.modelconfig.adapter.in.web.ModelConfigWebModels.AccessResponse;
import com.sleekydz86.lazylab.modelconfig.adapter.in.web.ModelConfigWebModels.StatusResponse;
import com.sleekydz86.lazylab.modelconfig.application.port.in.GetModelConfigStatusUseCase;
import com.sleekydz86.lazylab.modelconfig.application.port.in.UseModelConfigUseCase;

@RestController
@RequestMapping("/api/lazy-config")
public class ModelConfigController {

	private final GetModelConfigStatusUseCase getModelConfigStatusUseCase;
	private final UseModelConfigUseCase useModelConfigUseCase;

	public ModelConfigController(
		GetModelConfigStatusUseCase getModelConfigStatusUseCase,
		UseModelConfigUseCase useModelConfigUseCase
	) {
		this.getModelConfigStatusUseCase = getModelConfigStatusUseCase;
		this.useModelConfigUseCase = useModelConfigUseCase;
	}

	@GetMapping("/status")
	public StatusResponse status() {
		return StatusResponse.from(getModelConfigStatusUseCase.status());
	}

	@PostMapping("/use")
	public AccessResponse use() {
		return AccessResponse.from(useModelConfigUseCase.use());
	}
}
