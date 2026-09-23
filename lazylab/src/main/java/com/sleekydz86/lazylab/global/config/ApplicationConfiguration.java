package com.sleekydz86.lazylab.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sleekydz86.lazylab.global.lazy.LazyConstantModelConfigGateway;
import com.sleekydz86.lazylab.modelconfig.application.port.in.GetModelConfigStatusUseCase;
import com.sleekydz86.lazylab.modelconfig.application.port.in.UseModelConfigUseCase;
import com.sleekydz86.lazylab.modelconfig.application.port.out.LazyModelConfigGateway;
import com.sleekydz86.lazylab.modelconfig.application.port.out.ModelConfigLoader;
import com.sleekydz86.lazylab.modelconfig.application.service.ModelConfigService;

@Configuration
@EnableConfigurationProperties(LazyConfigProperties.class)
public class ApplicationConfiguration {

	@Bean
	LazyModelConfigGateway lazyModelConfigGateway(ModelConfigLoader loader) {
		return new LazyConstantModelConfigGateway(loader);
	}

	@Bean
	ModelConfigService modelConfigService(LazyModelConfigGateway gateway) {
		return new ModelConfigService(gateway);
	}

	@Bean
	UseModelConfigUseCase useModelConfigUseCase(ModelConfigService service) {
		return service;
	}

	@Bean
	GetModelConfigStatusUseCase getModelConfigStatusUseCase(ModelConfigService service) {
		return service;
	}
}
