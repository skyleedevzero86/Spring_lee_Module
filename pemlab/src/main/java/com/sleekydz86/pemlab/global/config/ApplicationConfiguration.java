package com.sleekydz86.pemlab.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sleekydz86.pemlab.pem.application.port.in.AnalyzePemUseCase;
import com.sleekydz86.pemlab.pem.application.port.in.GenerateKeyPairUseCase;
import com.sleekydz86.pemlab.pem.application.port.out.KeyPairFactoryPort;
import com.sleekydz86.pemlab.pem.application.port.out.PemInspectorPort;
import com.sleekydz86.pemlab.pem.application.service.AnalyzePemService;
import com.sleekydz86.pemlab.pem.application.service.GenerateKeyPairService;
import com.sleekydz86.pemlab.pem.domain.KeyGenerationSpec;

@Configuration
@EnableConfigurationProperties(KeyGenProperties.class)
public class ApplicationConfiguration {

	@Bean
	KeyGenerationSpec keyGenerationSpec(KeyGenProperties properties) {
		return new KeyGenerationSpec(properties.algorithm(), properties.keySize());
	}

	@Bean
	AnalyzePemUseCase analyzePemUseCase(PemInspectorPort pemInspectorPort) {
		return new AnalyzePemService(pemInspectorPort);
	}

	@Bean
	GenerateKeyPairUseCase generateKeyPairUseCase(
		KeyPairFactoryPort keyPairFactoryPort,
		KeyGenerationSpec keyGenerationSpec
	) {
		return new GenerateKeyPairService(keyPairFactoryPort, keyGenerationSpec);
	}
}
