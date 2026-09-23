package com.sleekydz86.pemlab.pem.application.service;

import com.sleekydz86.pemlab.pem.application.port.in.GenerateKeyPairUseCase;
import com.sleekydz86.pemlab.pem.application.port.out.KeyPairFactoryPort;
import com.sleekydz86.pemlab.pem.domain.GeneratedKeyPair;
import com.sleekydz86.pemlab.pem.domain.KeyGenerationSpec;

public final class GenerateKeyPairService implements GenerateKeyPairUseCase {

	private final KeyPairFactoryPort keyPairFactoryPort;
	private final KeyGenerationSpec defaultSpec;

	public GenerateKeyPairService(KeyPairFactoryPort keyPairFactoryPort, KeyGenerationSpec defaultSpec) {
		this.keyPairFactoryPort = keyPairFactoryPort;
		this.defaultSpec = defaultSpec;
	}

	@Override
	public GeneratedKeyPair generate() {
		return keyPairFactoryPort.create(defaultSpec);
	}
}
