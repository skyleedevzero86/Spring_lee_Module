package com.sleekydz86.catalogflow.global.exception;

import com.sleekydz86.catalogflow.domain.model.SupplierId;

public class SupplierNotFoundException extends ApplicationException {

	public SupplierNotFoundException(SupplierId supplierId) {
		super("공급사를 찾을 수 없습니다: " + supplierId.value());
	}
}
