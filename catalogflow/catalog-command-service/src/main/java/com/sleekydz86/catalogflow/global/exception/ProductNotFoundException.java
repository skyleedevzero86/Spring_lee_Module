package com.sleekydz86.catalogflow.global.exception;

import com.sleekydz86.catalogflow.domain.model.ProductId;

public class ProductNotFoundException extends ApplicationException {

	public ProductNotFoundException(ProductId productId) {
		super("상품을 찾을 수 없습니다: " + productId.value());
	}
}
