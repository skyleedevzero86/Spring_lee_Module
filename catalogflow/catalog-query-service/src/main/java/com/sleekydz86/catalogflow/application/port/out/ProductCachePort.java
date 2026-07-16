package com.sleekydz86.catalogflow.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;

public interface ProductCachePort {

	Optional<ProductView> getProduct(UUID productId);

	void putProduct(ProductView productView);

	void putProductMiss(UUID productId);

	boolean isProductMiss(UUID productId);

	Optional<ProductPageResult> getCategoryPage(UUID categoryId, String status, String cursor, int size);

	void putCategoryPage(UUID categoryId, String status, String cursor, int size, ProductPageResult pageResult);

	Optional<ProductPageResult> getPopular(int size);

	void putPopular(int size, ProductPageResult pageResult);

	void evictProductRelated(UUID productId, UUID categoryId);
}
