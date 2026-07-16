package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.application.port.out.SupplierRepository;
import com.sleekydz86.catalogflow.domain.model.SupplierId;
import org.springframework.stereotype.Component;

@Component
public class SupplierPersistenceAdapter implements SupplierRepository {

	private final SupplierJpaRepository supplierJpaRepository;

	public SupplierPersistenceAdapter(SupplierJpaRepository supplierJpaRepository) {
		this.supplierJpaRepository = supplierJpaRepository;
	}

	@Override
	public boolean existsById(SupplierId supplierId) {
		return supplierJpaRepository.existsById(supplierId.value());
	}
}
