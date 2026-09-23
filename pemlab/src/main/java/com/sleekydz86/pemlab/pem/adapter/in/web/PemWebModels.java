package com.sleekydz86.pemlab.pem.adapter.in.web;

import com.sleekydz86.pemlab.pem.domain.GeneratedKeyPair;
import com.sleekydz86.pemlab.pem.domain.PemInspection;
import com.sleekydz86.pemlab.pem.domain.PemText;

public final class PemWebModels {

	private PemWebModels() {
	}

	public record AnalyzeRequest(String pem) {

		public PemText toDomain() {
			return new PemText(pem);
		}
	}

	public record InspectionResponse(String type, String algorithm, String format, String size) {

		public static InspectionResponse from(PemInspection inspection) {
			return new InspectionResponse(
				inspection.type(),
				inspection.algorithm(),
				inspection.format(),
				inspection.sizeLabel()
			);
		}
	}

	public record GenerateResponse(
		String publicKeyPem,
		String privateKeyPem,
		InspectionResponse publicKey
	) {

		public static GenerateResponse from(GeneratedKeyPair pair) {
			return new GenerateResponse(
				pair.publicKeyPem().value(),
				pair.privateKeyPem().value(),
				InspectionResponse.from(pair.publicInspection())
			);
		}
	}
}
