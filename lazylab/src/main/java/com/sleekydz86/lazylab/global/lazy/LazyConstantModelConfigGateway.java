package com.sleekydz86.lazylab.global.lazy;

import java.util.concurrent.atomic.AtomicBoolean;

import com.sleekydz86.lazylab.global.util.Stopwatch;
import com.sleekydz86.lazylab.modelconfig.application.port.out.LazyModelConfigGateway;
import com.sleekydz86.lazylab.modelconfig.application.port.out.ModelConfigLoader;
import com.sleekydz86.lazylab.modelconfig.domain.ConfigAccessResult;
import com.sleekydz86.lazylab.modelconfig.domain.InitStatus;
import com.sleekydz86.lazylab.modelconfig.domain.ModelConfig;

public final class LazyConstantModelConfigGateway implements LazyModelConfigGateway {

	private final AtomicBoolean initialized = new AtomicBoolean(false);
	private final LazyConstant<ModelConfig> config;

	public LazyConstantModelConfigGateway(ModelConfigLoader loader) {
		this.config = LazyConstant.of(loader::load);
	}

	@Override
	public InitStatus status() {
		return initialized.get() ? InitStatus.INITIALIZED : InitStatus.NOT_INITIALIZED;
	}

	@Override
	public ConfigAccessResult access() {
		Stopwatch stopwatch = Stopwatch.start();
		ModelConfig modelConfig = config.get();
		long loadTimeMillis = stopwatch.elapsedMillis();
		boolean firstAccess = initialized.compareAndSet(false, true);
		return new ConfigAccessResult(
			InitStatus.INITIALIZED,
			firstAccess,
			loadTimeMillis,
			modelConfig
		);
	}
}
