package com.sleekydz86.catalogflow.application.port.out;

import com.sleekydz86.catalogflow.domain.model.Product;
import com.sleekydz86.catalogflow.domain.model.ProductId;

import java.util.Optional;

public interface ProductRepository {

	Optional<Product> findById(ProductId productId);

	boolean existsById(ProductId productId);

	void save(Product product);
}
