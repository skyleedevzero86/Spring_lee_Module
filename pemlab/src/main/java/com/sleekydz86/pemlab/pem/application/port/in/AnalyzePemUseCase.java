package com.sleekydz86.pemlab.pem.application.port.in;

import com.sleekydz86.pemlab.pem.domain.PemInspection;
import com.sleekydz86.pemlab.pem.domain.PemText;

public interface AnalyzePemUseCase {

	PemInspection analyze(PemText pemText);
}
