package com.sleekydz86.catalogflow.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;

public interface ProductViewStore {

	Optional<ProductView> findByProductId(UUID productId);

	void save(ProductView productView);

	List<ProductView> findByCriteria(ProductQueryCriteria criteria, int fetchSize);
}
