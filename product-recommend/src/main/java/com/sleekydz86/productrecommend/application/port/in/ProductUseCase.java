package com.sleekydz86.productrecommend.application.port.in;

import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.product.ProductStatus;
import com.sleekydz86.productrecommend.domain.recommend.RankedRecommendation;
import com.sleekydz86.productrecommend.domain.recommend.SearchCondition;
import com.sleekydz86.productrecommend.domain.user.BehaviorEvent;
import com.sleekydz86.productrecommend.domain.user.BehaviorEventType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductUseCase {

	Product create(
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock
	);

	Optional<Product> findById(Long id);

	List<Product> findAll();

	Product update(
		Long id,
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock,
		ProductStatus status
	);

	void delete(Long id);

	List<Product> knnSearch(List<String> keywords, int k);

	List<Product> hybridSearch(List<String> keywords, int k);

	List<Product> findSimilarProducts(Long productId, int k);

	SearchCondition understandQuery(String query, int limit);

	List<RankedRecommendation> recommend(String userId, String query, int limit);

	BehaviorEvent trackEvent(
		String userId,
		Long productId,
		BehaviorEventType type,
		String impressionId,
		Integer position,
		String query
	);

	RecommendationStats stats();

	int reindexMismatchedEmbeddings();

	int retryPendingEmbeddings();

	record RecommendationStats(long impressions, long clicks, double ctr) {
	}
}
