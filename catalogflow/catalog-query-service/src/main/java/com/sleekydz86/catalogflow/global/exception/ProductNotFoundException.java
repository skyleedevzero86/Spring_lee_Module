package com.sleekydz86.catalogflow.global.exception;

import java.util.UUID;

public class ProductNotFoundException extends ApplicationException {

	public ProductNotFoundException(UUID productId) {
		super("상품을 찾을 수 없습니다: " + productId);
	}
}
