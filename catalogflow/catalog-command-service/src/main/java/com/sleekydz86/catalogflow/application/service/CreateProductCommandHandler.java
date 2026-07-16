package com.sleekydz86.catalogflow.application.service;

import java.time.Clock;
import java.time.Instant;

import com.sleekydz86.catalogflow.application.command.CreateProductCommand;
import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.port.in.CreateProductUseCase;
import com.sleekydz86.catalogflow.application.port.out.SupplierRepository;
import com.sleekydz86.catalogflow.domain.model.CategoryId;
import com.sleekydz86.catalogflow.domain.model.Money;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductDescription;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.domain.model.ProductName;
import com.sleekydz86.catalogflow.domain.model.SupplierId;
import com.sleekydz86.catalogflow.global.exception.SupplierNotFoundException;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateProductCommandHandler implements CreateProductUseCase {

	private final SupplierRepository supplierRepository;
	private final ProductPersistenceCoordinator persistenceCoordinator;
	private final Clock clock;

	public CreateProductCommandHandler(
			SupplierRepository supplierRepository,
			ProductPersistenceCoordinator persistenceCoordinator,
			Clock clock) {
		this.supplierRepository = supplierRepository;
		this.persistenceCoordinator = persistenceCoordinator;
		this.clock = clock;
	}

	@Override
	public ProductCommandResult create(CreateProductCommand command) {
		SupplierId supplierId = new SupplierId(command.supplierId());
		if (!supplierRepository.existsById(supplierId)) {
			throw new SupplierNotFoundException(supplierId);
		}

		Instant now = clock.instant();
		Product product = Product.create(
				ProductId.generate(),
				new ProductName(command.name()),
				new ProductDescription(command.description()),
				new Money(command.priceAmount(), command.priceCurrency()),
				new CategoryId(command.categoryId()),
				supplierId,
				now,
				CorrelationIdHolder.getOrGenerate());

		persistenceCoordinator.save(product);
		return ProductCommandResultMapper.toResult(product);
	}
}
