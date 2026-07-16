package com.sleekydz86.catalogflow.application.service;

import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.model.ProductView;
import com.sleekydz86.catalogflow.application.port.in.GetProductQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListCategoryProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListPopularProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.SearchProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.out.ProductViewStore;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;
import com.sleekydz86.catalogflow.global.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ProductQueryService implements
		GetProductQueryUseCase,
		ListProductsQueryUseCase,
		SearchProductsQueryUseCase,
		ListCategoryProductsQueryUseCase,
		ListPopularProductsQueryUseCase {

	private final ProductViewStore productViewStore;

	public ProductQueryService(ProductViewStore productViewStore) {
		this.productViewStore = productViewStore;
	}

	@Override
	public ProductView getById(UUID productId) {
		return productViewStore.findByProductId(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));
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
		return toPage(criteria);
	}

	@Override
	public ProductPageResult listPopular(ProductQueryCriteria criteria) {
		return toPage(criteria);
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
		String publishedAt = view.getPublishedAt() == null ? "" : view.getPublishedAt().toString();
		return publishedAt + "|" + view.getProductId();
	}
}
