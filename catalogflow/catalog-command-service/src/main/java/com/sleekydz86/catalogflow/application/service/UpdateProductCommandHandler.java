package com.sleekydz86.catalogflow.application.service;

import java.time.Clock;
import java.time.Instant;

import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.command.UpdateProductCommand;
import com.sleekydz86.catalogflow.application.port.in.UpdateProductUseCase;
import com.sleekydz86.catalogflow.application.port.out.SupplierRepository;
import com.sleekydz86.catalogflow.domain.model.CategoryId;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductDescription;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductName;
import com.sleekydz86.catalogflow.domain.model.SupplierId;
import com.sleekydz86.catalogflow.global.exception.ProductNotFoundException;
import com.sleekydz86.catalogflow.global.exception.SupplierNotFoundException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateProductCommandHandler implements UpdateProductUseCase {

	private final ProductQuerySupport productQuerySupport;
	private final SupplierRepository supplierRepository;
	private final ProductPersistenceCoordinator persistenceCoordinator;
	private final Clock clock;

	public UpdateProductCommandHandler(
			ProductQuerySupport productQuerySupport,
			SupplierRepository supplierRepository,
			ProductPersistenceCoordinator persistenceCoordinator,
			Clock clock) {
		this.productQuerySupport = productQuerySupport;
		this.supplierRepository = supplierRepository;
		this.persistenceCoordinator = persistenceCoordinator;
		this.clock = clock;
	}

	@Override
	public ProductCommandResult update(UpdateProductCommand command) {
		ProductId productId = new ProductId(command.productId());
		Product product = productQuerySupport.findProductOrThrow(productId);

		SupplierId supplierId = new SupplierId(command.supplierId());
		if (!supplierRepository.existsById(supplierId)) {
			throw new SupplierNotFoundException(supplierId);
		}

		Instant now = clock.instant();
		product.updateDraft(
				command.expectedVersion(),
				new ProductName(command.name()),
				new ProductDescription(command.description()),
				new CategoryId(command.categoryId()),
				supplierId,
				now,
				CorrelationIdHolder.getOrGenerate());

		persistenceCoordinator.save(product);
		return ProductCommandResultMapper.toResult(product);
	}
}
