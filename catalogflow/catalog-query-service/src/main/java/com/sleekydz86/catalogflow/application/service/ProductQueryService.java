package com.sleekydz86.catalogflow.application.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.in.GetProductQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListCategoryProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListPopularProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.SearchProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.out.ProductCachePort;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;
import com.sleekydz86.catalogflow.global.exception.ProductNotFoundException;
import com.sleekydz86.catalogflow.global.metrics.CatalogQueryMetrics;
import org.springframework.stereotype.Service;

@Service
public class ProductQueryService implements
		GetProductQueryUseCase,
		ListProductsQueryUseCase,
		SearchProductsQueryUseCase,
		ListCategoryProductsQueryUseCase,
		ListPopularProductsQueryUseCase {

	private final ProductViewStore productViewStore;
	private final ProductCachePort productCachePort;
	private final CatalogQueryMetrics catalogQueryMetrics;
	private final ConcurrentHashMap<String, ReentrantLock> loadLocks = new ConcurrentHashMap<>();

	public ProductQueryService(
			ProductViewStore productViewStore,
			ProductCachePort productCachePort,
			CatalogQueryMetrics catalogQueryMetrics) {
		this.productViewStore = productViewStore;
		this.productCachePort = productCachePort;
		this.catalogQueryMetrics = catalogQueryMetrics;
	}

	@Override
	public ProductView getById(UUID productId) {
		catalogQueryMetrics.incrementProductQueried();
		if (productCachePort.isProductMiss(productId)) {
			catalogQueryMetrics.incrementCacheHit();
			throw new ProductNotFoundException(productId);
		}
		Optional<ProductView> cached = productCachePort.getProduct(productId);
		if (cached.isPresent()) {
			catalogQueryMetrics.incrementCacheHit();
			return cached.get();
		}
		catalogQueryMetrics.incrementCacheMiss();
		return loadProduct(productId);
	}

	@Override
	public ProductPageResult list(ProductQueryCriteria criteria) {
		return toPage(criteria);
	}

	@Override
	public ProductPageResult search(ProductQueryCriteria criteria) {
		return toPage(criteria);
	}

	@Override
	public ProductPageResult listByCategory(ProductQueryCriteria criteria) {
		String cursor = encodeCursorValue(criteria.cursorPublishedAt(), criteria.cursorProductId());
		Optional<ProductPageResult> cached = productCachePort.getCategoryPage(
				criteria.categoryId(),
				criteria.status(),
				cursor,
				criteria.size());
		if (cached.isPresent()) {
			return cached.get();
		}
		ProductPageResult page = toPage(criteria);
		productCachePort.putCategoryPage(
				criteria.categoryId(),
				criteria.status(),
				cursor,
				criteria.size(),
				page);
		return page;
	}

	@Override
	public ProductPageResult listPopular(ProductQueryCriteria criteria) {
		Optional<ProductPageResult> cached = productCachePort.getPopular(criteria.size());
		if (cached.isPresent()) {
			return cached.get();
		}
		ProductPageResult page = toPage(criteria);
		productCachePort.putPopular(criteria.size(), page);
		return page;
	}

	private ProductView loadProduct(UUID productId) {
		ReentrantLock lock = loadLocks.computeIfAbsent(productId.toString(), key -> new ReentrantLock());
		lock.lock();
		try {
			if (productCachePort.isProductMiss(productId)) {
				throw new ProductNotFoundException(productId);
			}
			Optional<ProductView> cached = productCachePort.getProduct(productId);
			if (cached.isPresent()) {
				return cached.get();
			}
			Optional<ProductView> stored = productViewStore.findByProductId(productId);
			if (stored.isPresent()) {
				productCachePort.putProduct(stored.get());
				return stored.get();
			}
			productCachePort.putProductMiss(productId);
			throw new ProductNotFoundException(productId);
		}
		finally {
			lock.unlock();
			loadLocks.remove(productId.toString(), lock);
		}
	}

	private ProductPageResult toPage(ProductQueryCriteria criteria) {
		int fetchSize = criteria.size() + 1;
		List<ProductView> fetched = productViewStore.findByCriteria(criteria, fetchSize);
		boolean hasNext = fetched.size() > criteria.size();
		List<ProductView> items = hasNext ? fetched.subList(0, criteria.size()) : fetched;
		String nextCursor = null;
		if (hasNext && !items.isEmpty()) {
			ProductView last = items.get(items.size() - 1);
			nextCursor = encodeCursor(last);
		}
		return new ProductPageResult(List.copyOf(items), nextCursor, hasNext);
	}

	private String encodeCursor(ProductView view) {
		return encodeCursorValue(
				view.getPublishedAt(),
				view.getProductId() == null ? null : view.getProductId().toString());
	}

	private String encodeCursorValue(java.time.Instant publishedAt, String productId) {
		if (publishedAt == null && (productId == null || productId.isBlank())) {
			return null;
		}
		String publishedAtText = publishedAt == null ? "" : publishedAt.toString();
		String productIdText = productId == null ? "" : productId;
		return publishedAtText + "|" + productIdText;
	}
}
