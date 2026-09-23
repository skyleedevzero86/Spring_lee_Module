package com.sleekydz86.pemlab.pem.domain;

public record GeneratedKeyPair(PemText publicKeyPem, PemText privateKeyPem, PemInspection publicInspection) {

	public GeneratedKeyPair {
		if (publicKeyPem == null) {
			throw new IllegalArgumentException("공개키 PEM이 필요합니다");
		}
		if (privateKeyPem == null) {
			throw new IllegalArgumentException("개인키 PEM이 필요합니다");
		}
		if (publicInspection == null) {
			throw new IllegalArgumentException("공개키 분석 결과가 필요합니다");
		}
	}
}
