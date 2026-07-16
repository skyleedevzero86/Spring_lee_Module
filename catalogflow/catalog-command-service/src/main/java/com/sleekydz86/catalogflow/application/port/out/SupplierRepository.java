package com.sleekydz86.catalogflow.application.port.out;

import com.sleekydz86.catalogflow.domain.model.SupplierId;

public interface SupplierRepository {

	boolean existsById(SupplierId supplierId);
}
