package com.sleekydz86.catalogflow.application.service;

import java.util.List;

import com.sleekydz86.catalogflow.application.port.out.OutboxEventPort;
import com.sleekydz86.catalogflow.application.port.out.ProductRepository;
import com.sleekydz86.catalogflow.domain.event.DomainEvent;
import com.sleekydz86.catalogflow.domain.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceCoordinator {

	private final ProductRepository productRepository;
	private final OutboxEventPort outboxEventPort;

	public ProductPersistenceCoordinator(ProductRepository productRepository, OutboxEventPort outboxEventPort) {
		this.productRepository = productRepository;
		this.outboxEventPort = outboxEventPort;
	}

	public void save(Product product) {
		productRepository.save(product);
		List<DomainEvent> events = product.pullDomainEvents();
		outboxEventPort.saveAll(events);
	}
}
