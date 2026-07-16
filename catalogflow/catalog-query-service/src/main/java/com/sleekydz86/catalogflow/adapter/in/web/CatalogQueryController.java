package com.sleekydz86.catalogflow.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.sleekydz86.catalogflow.adapter.in.web.dto.ProductPageResponse;
import com.sleekydz86.catalogflow.adapter.in.web.dto.ProductViewResponse;
import com.sleekydz86.catalogflow.application.port.in.GetProductQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListCategoryProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListPopularProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.ListProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.port.in.SearchProductsQueryUseCase;
import com.sleekydz86.catalogflow.application.query.ProductPageResult;
import com.sleekydz86.catalogflow.application.query.ProductQueryCriteria;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogQueryController {

	private final GetProductQueryUseCase getProductQueryUseCase;
	private final ListProductsQueryUseCase listProductsQueryUseCase;
	private final SearchProductsQueryUseCase searchProductsQueryUseCase;
	private final ListCategoryProductsQueryUseCase listCategoryProductsQueryUseCase;
	private final ListPopularProductsQueryUseCase listPopularProductsQueryUseCase;

	public CatalogQueryController(
			GetProductQueryUseCase getProductQueryUseCase,
			ListProductsQueryUseCase listProductsQueryUseCase,
			SearchProductsQueryUseCase searchProductsQueryUseCase,
			ListCategoryProductsQueryUseCase listCategoryProductsQueryUseCase,
			ListPopularProductsQueryUseCase listPopularProductsQueryUseCase) {
		this.getProductQueryUseCase = getProductQueryUseCase;
		this.listProductsQueryUseCase = listProductsQueryUseCase;
		this.searchProductsQueryUseCase = searchProductsQueryUseCase;
		this.listCategoryProductsQueryUseCase = listCategoryProductsQueryUseCase;
		this.listPopularProductsQueryUseCase = listPopularProductsQueryUseCase;
	}

	@GetMapping("/products/{productId}")
	public ProductViewResponse getProduct(@PathVariable UUID productId) {
		return ProductViewResponse.from(getProductQueryUseCase.getById(productId));
	}

	@GetMapping("/products")
	public ProductPageResponse listProducts(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") int size) {
		CursorParts cursorParts = parseCursor(cursor);
		ProductPageResult result = listProductsQueryUseCase.list(
				ProductQueryCriteria.list(status, cursorParts.publishedAt(), cursorParts.productId(), size));
		return toResponse(result);
	}

	@GetMapping("/products/search")
	public ProductPageResponse searchProducts(
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) BigDecimal minPrice,
			@RequestParam(required = false) BigDecimal maxPrice,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") int size) {
		CursorParts cursorParts = parseCursor(cursor);
		ProductPageResult result = searchProductsQueryUseCase.search(
				ProductQueryCriteria.search(
						name,
						keyword,
						status,
						minPrice,
						maxPrice,
						cursorParts.publishedAt(),
						cursorParts.productId(),
						size));
		return toResponse(result);
	}

	@GetMapping("/products/popular")
	public ProductPageResponse listPopularProducts(@RequestParam(defaultValue = "10") int size) {
		ProductPageResult result = listPopularProductsQueryUseCase.listPopular(ProductQueryCriteria.popular(size));
		return toResponse(result);
	}

	@GetMapping("/categories/{categoryId}/products")
	public ProductPageResponse listCategoryProducts(
			@PathVariable UUID categoryId,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String cursor,
			@RequestParam(defaultValue = "20") int size) {
		CursorParts cursorParts = parseCursor(cursor);
		ProductPageResult result = listCategoryProductsQueryUseCase.listByCategory(
				ProductQueryCriteria.category(
						categoryId,
						status,
						cursorParts.publishedAt(),
						cursorParts.productId(),
						size));
		return toResponse(result);
	}

	private ProductPageResponse toResponse(ProductPageResult result) {
		return new ProductPageResponse(
				result.items().stream().map(ProductViewResponse::from).toList(),
				result.nextCursor(),
				result.hasNext());
	}

	private CursorParts parseCursor(String cursor) {
		if (cursor == null || cursor.isBlank()) {
			return new CursorParts(null, null);
		}
		String[] parts = cursor.split("\\|", 2);
		if (parts.length != 2) {
			throw new IllegalArgumentException("커서 형식이 올바르지 않습니다");
		}
		try {
			Instant publishedAt = parts[0].isBlank() ? null : Instant.parse(parts[0]);
			String productId = parts[1].isBlank() ? null : parts[1];
			return new CursorParts(publishedAt, productId);
		}
		catch (Exception exception) {
			throw new IllegalArgumentException("커서 형식이 올바르지 않습니다");
		}
	}

	private record CursorParts(Instant publishedAt, String productId) {
	}
}
