package com.sleekydz86.catalogflow.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;

public interface ProductViewStore {

	Optional<ProductView> findByProductId(UUID productId);

	void save(ProductView productView);
}
