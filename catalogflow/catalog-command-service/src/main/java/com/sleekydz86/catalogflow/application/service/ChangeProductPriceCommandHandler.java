package com.sleekydz86.catalogflow.application.service;

import java.time.Clock;
import java.time.Instant;

import com.sleekydz86.catalogflow.application.command.ChangeProductPriceCommand;
import com.sleekydz86.catalogflow.application.command.ProductCommandResult;
import com.sleekydz86.catalogflow.application.port.in.ChangeProductPriceUseCase;
import com.sleekydz86.catalogflow.domain.model.Money;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import com.sleekydz86.catalogflow.global.util.CorrelationIdHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ChangeProductPriceCommandHandler implements ChangeProductPriceUseCase {

	private final ProductQuerySupport productQuerySupport;
	private final ProductPersistenceCoordinator persistenceCoordinator;
	private final Clock clock;

	public ChangeProductPriceCommandHandler(
			ProductQuerySupport productQuerySupport,
			ProductPersistenceCoordinator persistenceCoordinator,
			Clock clock) {
		this.productQuerySupport = productQuerySupport;
		this.persistenceCoordinator = persistenceCoordinator;
		this.clock = clock;
	}

	@Override
	public ProductCommandResult changePrice(ChangeProductPriceCommand command) {
		ProductId productId = new ProductId(command.productId());
		Product product = productQuerySupport.findProductOrThrow(productId);

		Instant now = clock.instant();
		product.changePrice(
				command.expectedVersion(),
				new Money(command.priceAmount(), command.priceCurrency()),
				now,
				CorrelationIdHolder.getOrGenerate());

		persistenceCoordinator.save(product);
		return ProductCommandResultMapper.toResult(product);
	}
}
