package com.sleekydz86.catalogflow.adapter.out.cache;

import java.util.Optional;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.out.ProductCachePort;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.cache.enabled", havingValue = "false")
public class NoOpProductCacheAdapter implements ProductCachePort {

	@Override
	public Optional<ProductView> getProduct(UUID productId) {
		return Optional.empty();
	}

	@Override
	public void putProduct(ProductView productView) {
	}

	@Override
	public void putProductMiss(UUID productId) {
	}

	@Override
	public boolean isProductMiss(UUID productId) {
		return false;
	}

	@Override
	public Optional<ProductPageResult> getCategoryPage(UUID categoryId, String status, String cursor, int size) {
		return Optional.empty();
	}

	@Override
	public void putCategoryPage(
			UUID categoryId,
			String status,
			String cursor,
			int size,
			ProductPageResult pageResult) {
	}

	@Override
	public Optional<ProductPageResult> getPopular(int size) {
		return Optional.empty();
	}

	@Override
	public void putPopular(int size, ProductPageResult pageResult) {
	}

	@Override
	public void evictProductRelated(UUID productId, UUID categoryId) {
	}
}
