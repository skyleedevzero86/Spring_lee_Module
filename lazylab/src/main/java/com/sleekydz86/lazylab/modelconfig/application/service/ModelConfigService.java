package com.sleekydz86.lazylab.modelconfig.application.service;

import com.sleekydz86.lazylab.modelconfig.application.port.in.GetModelConfigStatusUseCase;
import com.sleekydz86.lazylab.modelconfig.application.port.in.UseModelConfigUseCase;
import com.sleekydz86.lazylab.modelconfig.application.port.out.LazyModelConfigGateway;
import com.sleekydz86.lazylab.modelconfig.domain.ConfigAccessResult;
import com.sleekydz86.lazylab.modelconfig.domain.ConfigStatus;

public final class ModelConfigService implements UseModelConfigUseCase, GetModelConfigStatusUseCase {

	private final LazyModelConfigGateway gateway;

	public ModelConfigService(LazyModelConfigGateway gateway) {
		this.gateway = gateway;
	}

	@Override
	public ConfigAccessResult use() {
		return gateway.access();
	}

	@Override
	public ConfigStatus status() {
		return new ConfigStatus(gateway.status());
	}
}
