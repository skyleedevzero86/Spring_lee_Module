package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import com.sleekydz86.productrecommend.application.port.out.EmbeddingPort;
import com.sleekydz86.productrecommend.application.port.out.ProductRepositoryPort;
import com.sleekydz86.productrecommend.domain.product.EmbeddingStatus;
import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.product.ProductEmbeddingRequested;
import com.sleekydz86.productrecommend.domain.recommend.RrfMerger;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductApplicationService implements ProductUseCase {

	private final ProductRepositoryPort productRepositoryPort;
	private final EmbeddingPort embeddingPort;
	private final ApplicationEventPublisher eventPublisher;

	public ProductApplicationService(
		ProductRepositoryPort productRepositoryPort,
		EmbeddingPort embeddingPort,
		ApplicationEventPublisher eventPublisher
	) {
		this.productRepositoryPort = productRepositoryPort;
		this.embeddingPort = embeddingPort;
		this.eventPublisher = eventPublisher;
	}

	@Override
	@Transactional
	public Product create(String name, List<String> keywords) {
		List<String> safeKeywords = normalizeKeywords(keywords);
		Product saved = productRepositoryPort.save(Product.create(name, safeKeywords));
		if (!safeKeywords.isEmpty()) {
			eventPublisher.publishEvent(new ProductEmbeddingRequested(saved.id()));
		}
		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Product> findById(Long id) {
		return productRepositoryPort.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> findAll() {
		return productRepositoryPort.findAll();
	}

	@Override
	@Transactional
	public Product update(Long id, String name, List<String> keywords) {
		Product existing = productRepositoryPort.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
		List<String> safeKeywords = normalizeKeywords(keywords);
		Product updated = productRepositoryPort.save(existing.withNameAndKeywords(name, safeKeywords));
		if (!safeKeywords.isEmpty()) {
			eventPublisher.publishEvent(new ProductEmbeddingRequested(updated.id()));
		}
		return updated;
	}

	@Override
	@Transactional
	public void delete(Long id) {
		productRepositoryPort.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> knnSearch(List<String> keywords, int k) {
		List<String> safeKeywords = normalizeKeywords(keywords);
		if (safeKeywords.isEmpty()) {
			return List.of();
		}
		float[] queryVector = embeddingPort.average(embeddingPort.embedAll(safeKeywords));
		return productRepositoryPort.searchByEmbeddingNear(queryVector, normalizeTopK(k));
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> hybridSearch(List<String> keywords, int k) {
		List<String> safeKeywords = normalizeKeywords(keywords);
		if (safeKeywords.isEmpty()) {
			return List.of();
		}
		int topK = normalizeTopK(k);
		List<Product> vectorHits = knnSearch(safeKeywords, topK * 2);
		String keywordQuery = String.join(" ", safeKeywords);
		List<Product> keywordHits = productRepositoryPort.searchByNameTrigram(keywordQuery, topK * 2);

		List<Long> vectorIds = vectorHits.stream().map(Product::id).toList();
		List<Long> keywordIds = keywordHits.stream().map(Product::id).toList();
		List<RrfMerger.ScoredId> merged = RrfMerger.merge(List.of(vectorIds, keywordIds), topK);

		Map<Long, Product> byId = new LinkedHashMap<>();
		vectorHits.forEach(p -> byId.put(p.id(), p));
		keywordHits.forEach(p -> byId.putIfAbsent(p.id(), p));
		return merged.stream()
			.map(scored -> byId.get(scored.id()))
			.filter(Objects::nonNull)
			.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Product> findSimilarProducts(Long productId, int k) {
		Product product = productRepositoryPort.findById(productId)
			.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));
		if (product.embedding() != null) {
			return productRepositoryPort.searchByEmbeddingNear(product.embedding(), normalizeTopK(k) + 1).stream()
				.filter(p -> !Objects.equals(p.id(), productId))
				.limit(normalizeTopK(k))
				.toList();
		}
		return knnSearch(product.keywords(), normalizeTopK(k) + 1).stream()
			.filter(p -> !Objects.equals(p.id(), productId))
			.limit(normalizeTopK(k))
			.toList();
	}

	@Override
	@Transactional
	public int reindexMismatchedEmbeddings() {
		String currentModel = embeddingPort.modelName();
		List<Product> mismatched = productRepositoryPort.findByEmbeddingModelNot(currentModel);
		for (Product product : mismatched) {
			productRepositoryPort.save(product.withEmbedding(null, null, EmbeddingStatus.PENDING));
			eventPublisher.publishEvent(new ProductEmbeddingRequested(product.id()));
		}
		return mismatched.size();
	}

	@Override
	@Transactional
	public int retryPendingEmbeddings() {
		List<Product> pending = new ArrayList<>();
		pending.addAll(productRepositoryPort.findByEmbeddingStatus(EmbeddingStatus.PENDING));
		pending.addAll(productRepositoryPort.findByEmbeddingStatus(EmbeddingStatus.FAILED));
		pending.addAll(productRepositoryPort.findByEmbeddingIsNull());
		List<Long> ids = pending.stream()
			.filter(p -> !p.keywords().isEmpty())
			.map(Product::id)
			.distinct()
			.collect(Collectors.toList());
		for (Long id : ids) {
			eventPublisher.publishEvent(new ProductEmbeddingRequested(id));
		}
		return ids.size();
	}

	private static List<String> normalizeKeywords(List<String> keywords) {
		if (keywords == null) {
			return List.of();
		}
		return keywords.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toList();
	}

	private static int normalizeTopK(int k) {
		return Math.max(1, Math.min(k <= 0 ? 10 : k, 50));
	}
}
