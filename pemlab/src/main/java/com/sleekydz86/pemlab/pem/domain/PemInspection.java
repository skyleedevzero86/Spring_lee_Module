package com.sleekydz86.pemlab.pem.domain;

public record PemInspection(
	String type,
	String algorithm,
	String format,
	String sizeLabel
) {

	public PemInspection {
		if (type == null || type.isBlank()) {
			throw new IllegalArgumentException("PEM 유형이 필요합니다");
		}
		if (algorithm == null || algorithm.isBlank()) {
			throw new IllegalArgumentException("알고리즘이 필요합니다");
		}
		if (format == null || format.isBlank()) {
			throw new IllegalArgumentException("포맷이 필요합니다");
		}
		if (sizeLabel == null || sizeLabel.isBlank()) {
			throw new IllegalArgumentException("키 크기 정보가 필요합니다");
		}
	}
}
