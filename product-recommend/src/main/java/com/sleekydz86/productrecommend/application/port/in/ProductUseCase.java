package com.sleekydz86.productrecommend.application.port.in;

import com.sleekydz86.productrecommend.domain.product.Product;

import java.util.List;
import java.util.Optional;

public interface ProductUseCase {

	Product create(String name, List<String> keywords);

	Optional<Product> findById(Long id);

	List<Product> findAll();

	Product update(Long id, String name, List<String> keywords);

	void delete(Long id);

	List<Product> knnSearch(List<String> keywords, int k);

	List<Product> hybridSearch(List<String> keywords, int k);

	List<Product> findSimilarProducts(Long productId, int k);

	int reindexMismatchedEmbeddings();

	int retryPendingEmbeddings();
}
