package com.sleekydz86.catalogflow.domain.exception;

public class ProductVersionConflictException extends DomainException {

	public ProductVersionConflictException(long expectedVersion, long actualVersion) {
		super("상품 버전이 충돌했습니다. 기대 버전=" + expectedVersion + ", 실제 버전=" + actualVersion);
	}
}
