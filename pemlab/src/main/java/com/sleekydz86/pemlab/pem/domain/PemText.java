package com.sleekydz86.pemlab.pem.domain;

public record PemText(String value) {

	public PemText {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException("PEM 텍스트가 필요합니다");
		}
		value = value.trim();
	}
}
