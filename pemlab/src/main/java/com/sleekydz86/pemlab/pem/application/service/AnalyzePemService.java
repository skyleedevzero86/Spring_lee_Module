package com.sleekydz86.pemlab.pem.application.service;

import com.sleekydz86.pemlab.pem.application.port.in.AnalyzePemUseCase;
import com.sleekydz86.pemlab.pem.application.port.out.PemInspectorPort;
import com.sleekydz86.pemlab.pem.domain.PemInspection;
import com.sleekydz86.pemlab.pem.domain.PemText;

public final class AnalyzePemService implements AnalyzePemUseCase {

	private final PemInspectorPort pemInspectorPort;

	public AnalyzePemService(PemInspectorPort pemInspectorPort) {
		this.pemInspectorPort = pemInspectorPort;
	}

	@Override
	public PemInspection analyze(PemText pemText) {
		return pemInspectorPort.inspect(pemText);
	}
}
