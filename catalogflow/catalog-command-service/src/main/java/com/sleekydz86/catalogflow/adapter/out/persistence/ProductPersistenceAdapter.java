package com.sleekydz86.catalogflow.adapter.out.persistence;

import com.sleekydz86.catalogflow.adapter.out.persistence.entity.ProductEntity;
import com.sleekydz86.catalogflow.application.port.out.ProductRepository;
import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class ProductPersistenceAdapter implements ProductRepository {

	private final ProductJpaRepository productJpaRepository;

	public ProductPersistenceAdapter(ProductJpaRepository productJpaRepository) {
		this.productJpaRepository = productJpaRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Product> findById(ProductId productId) {
		return productJpaRepository.findWithDetailsById(productId.value())
				.map(ProductMapper::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsById(ProductId productId) {
		return productJpaRepository.existsActiveById(productId.value());
	}

	@Override
	@Transactional
	public void save(Product product) {
		ProductEntity entity = productJpaRepository.findWithDetailsById(product.getId().value())
				.map(existing -> {
					ProductMapper.applyChanges(product, existing);
					return existing;
				})
				.orElseGet(() -> ProductMapper.toNewEntity(product));

		productJpaRepository.saveAndFlush(entity);
	}
}
