package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import com.sleekydz86.productrecommend.application.port.out.BehaviorEventPort;
import com.sleekydz86.productrecommend.application.port.out.EmbeddingPort;
import com.sleekydz86.productrecommend.application.port.out.OutboxPort;
import com.sleekydz86.productrecommend.application.port.out.ProductRepositoryPort;
import com.sleekydz86.productrecommend.application.port.out.UserPreferencePort;
import com.sleekydz86.productrecommend.domain.outbox.OutboxMessage;
import com.sleekydz86.productrecommend.domain.product.EmbeddingStatus;
import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.product.ProductStatus;
import com.sleekydz86.productrecommend.domain.recommend.HeuristicReranker;
import com.sleekydz86.productrecommend.domain.recommend.MmrSelector;
import com.sleekydz86.productrecommend.domain.recommend.PersonalizedScorer;
import com.sleekydz86.productrecommend.domain.recommend.ProductSearchFilter;
import com.sleekydz86.productrecommend.domain.recommend.RankedRecommendation;
import com.sleekydz86.productrecommend.domain.recommend.RuleBasedQueryUnderstanding;
import com.sleekydz86.productrecommend.domain.recommend.RrfMerger;
import com.sleekydz86.productrecommend.domain.recommend.SearchCondition;
import com.sleekydz86.productrecommend.domain.user.BehaviorEvent;
import com.sleekydz86.productrecommend.domain.user.BehaviorEventType;
import com.sleekydz86.productrecommend.domain.user.UserPreference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductApplicationService implements ProductUseCase {

	private final ProductRepositoryPort productRepositoryPort;
	private final EmbeddingPort embeddingPort;
	private final OutboxPort outboxPort;
	private final UserPreferencePort userPreferencePort;
	private final BehaviorEventPort behaviorEventPort;

	public ProductApplicationService(
		ProductRepositoryPort productRepositoryPort,
		EmbeddingPort embeddingPort,
		OutboxPort outboxPort,
		UserPreferencePort userPreferencePort,
		BehaviorEventPort behaviorEventPort
	) {
		this.productRepositoryPort = productRepositoryPort;
		this.embeddingPort = embeddingPort;
		this.outboxPort = outboxPort;
		this.userPreferencePort = userPreferencePort;
		this.behaviorEventPort = behaviorEventPort;
	}

	@Override
	@Transactional
	public Product create(
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock
	) {
		List<String> safeKeywords = normalizeKeywords(keywords);
		Product saved = productRepositoryPort.save(
			Product.create(name, safeKeywords, category, brand, color, price, stock)
		);
		enqueueEmbedding(saved.id());
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
	public Product update(
		Long id,
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock,
		ProductStatus status
	) {
		Product existing = productRepositoryPort.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + id));
		List<String> safeKeywords = normalizeKeywords(keywords);
		Product updated = productRepositoryPort.save(existing.withCatalog(
			name, safeKeywords, category, brand, color, price, stock, status == null ? ProductStatus.ACTIVE : status
		));
		enqueueEmbedding(updated.id());
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
		return mergeByRrf(vectorHits, keywordHits, topK);
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
	public SearchCondition understandQuery(String query, int limit) {
		return RuleBasedQueryUnderstanding.parse(query, limit);
	}

	@Override
	@Transactional
	public List<RankedRecommendation> recommend(String userId, String query, int limit) {
		String safeUserId = (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
		SearchCondition condition = understandQuery(query, Math.max(limit * 4, 20));
		UserPreference preference = userPreferencePort.findByUserId(safeUserId)
			.orElseGet(() -> UserPreference.empty(safeUserId));

		List<Product> candidates;
		if (isColdStartUser(preference)) {
			candidates = coldStartCandidates(condition);
		} else {
			candidates = personalizedCandidates(condition);
		}

		float[] queryVector = embeddingPort.embed(condition.semanticQuery());
		Instant now = Instant.now();
		List<RankedRecommendation> scored = new ArrayList<>();
		for (Product product : candidates) {
			if (!product.available()) {
				continue;
			}
			double vectorSim = product.embedding() == null ? 0.2 : cosine(queryVector, product.embedding());
			double keywordScore = keywordOverlap(condition.semanticQuery(), product);
			scored.add(PersonalizedScorer.score(product, vectorSim, keywordScore, condition, preference, now));
		}

		List<RankedRecommendation> diversified = MmrSelector.select(scored, Math.max(limit * 2, 10), 0.7);
		List<RankedRecommendation> reranked = HeuristicReranker.rerank(diversified, normalizeTopK(limit));

		String impressionId = UUID.randomUUID().toString();
		for (int i = 0; i < reranked.size(); i++) {
			RankedRecommendation item = reranked.get(i);
			behaviorEventPort.save(BehaviorEvent.of(
				safeUserId,
				item.product().id(),
				BehaviorEventType.RECOMMENDATION_IMPRESSION,
				impressionId,
				i + 1,
				query
			));
		}
		return reranked;
	}

	@Override
	@Transactional
	public BehaviorEvent trackEvent(
		String userId,
		Long productId,
		BehaviorEventType type,
		String impressionId,
		Integer position,
		String query
	) {
		String safeUserId = (userId == null || userId.isBlank()) ? "anonymous" : userId.trim();
		BehaviorEvent saved = behaviorEventPort.save(
			BehaviorEvent.of(safeUserId, productId, type, impressionId, position, query)
		);
		updatePreference(safeUserId, productId, type);
		if (productId != null && (type == BehaviorEventType.CLICK
			|| type == BehaviorEventType.RECOMMENDATION_CLICK
			|| type == BehaviorEventType.WISH
			|| type == BehaviorEventType.CART
			|| type == BehaviorEventType.PURCHASE)) {
			productRepositoryPort.findById(productId).ifPresent(product -> {
				double bump = switch (type) {
					case PURCHASE -> 5;
					case CART -> 3;
					case WISH -> 2;
					default -> 1;
				};
				productRepositoryPort.save(product.withPopularity(product.popularityScore() + bump));
			});
		}
		outboxPort.save(OutboxMessage.pending(
			"BehaviorEvent",
			saved.eventId(),
			type.name(),
			"{\"userId\":\"" + safeUserId + "\",\"productId\":" + productId + "}"
		));
		return saved;
	}

	@Override
	@Transactional(readOnly = true)
	public RecommendationStats stats() {
		long impressions = behaviorEventPort.countRecommendationImpressions();
		long clicks = behaviorEventPort.countRecommendationClicks();
		double ctr = impressions == 0 ? 0 : (double) clicks / impressions;
		return new RecommendationStats(impressions, clicks, ctr);
	}

	@Override
	@Transactional
	public int reindexMismatchedEmbeddings() {
		String currentModel = embeddingPort.modelName();
		List<Product> mismatched = productRepositoryPort.findByEmbeddingModelNot(currentModel);
		for (Product product : mismatched) {
			productRepositoryPort.save(product.withEmbedding(null, null, EmbeddingStatus.PENDING));
			enqueueEmbedding(product.id());
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
			enqueueEmbedding(id);
		}
		return ids.size();
	}

	private void enqueueEmbedding(Long productId) {
		outboxPort.save(OutboxMessage.pending(
			"Product",
			String.valueOf(productId),
			"PRODUCT_EMBEDDING_REQUESTED",
			"{\"productId\":" + productId + "}"
		));
	}

	private List<Product> personalizedCandidates(SearchCondition condition) {
		ProductSearchFilter filter = new ProductSearchFilter(
			condition.category(),
			condition.brand(),
			condition.excludeBrand(),
			condition.color(),
			condition.minPrice(),
			condition.maxPrice(),
			true
		);
		float[] queryVector = embeddingPort.embed(condition.semanticQuery());
		List<Product> vectorHits = productRepositoryPort.searchByEmbeddingNearFiltered(queryVector, filter, 50);
		List<Product> keywordHits = productRepositoryPort.searchByNameTrigram(condition.semanticQuery(), 50).stream()
			.filter(p -> matchesFilter(p, filter))
			.toList();
		return mergeByRrf(vectorHits, keywordHits, 50);
	}

	private List<Product> coldStartCandidates(SearchCondition condition) {
		List<Product> popular = productRepositoryPort.findPopular(20);
		List<Product> semantic = personalizedCandidates(condition);
		Map<Long, Product> merged = new LinkedHashMap<>();
		semantic.forEach(p -> merged.put(p.id(), p));
		popular.forEach(p -> merged.putIfAbsent(p.id(), p));
		return new ArrayList<>(merged.values());
	}

	private boolean isColdStartUser(UserPreference preference) {
		return preference.preferredCategory() == null
			&& preference.preferredBrand() == null
			&& preference.embedding() == null;
	}

	private void updatePreference(String userId, Long productId, BehaviorEventType type) {
		if (productId == null) {
			return;
		}
		if (type != BehaviorEventType.VIEW
			&& type != BehaviorEventType.CLICK
			&& type != BehaviorEventType.WISH
			&& type != BehaviorEventType.CART
			&& type != BehaviorEventType.PURCHASE
			&& type != BehaviorEventType.RECOMMENDATION_CLICK
			&& type != BehaviorEventType.FEEDBACK_UP) {
			return;
		}
		productRepositoryPort.findById(productId).ifPresent(product -> {
			UserPreference current = userPreferencePort.findByUserId(userId).orElseGet(() -> UserPreference.empty(userId));
			UserPreference updated = current;
			if (product.category() != null) {
				updated = updated.withCategory(product.category());
			}
			if (product.brand() != null) {
				updated = updated.withBrand(product.brand());
			}
			if (product.price() != null) {
				BigDecimal min = product.price().multiply(BigDecimal.valueOf(0.7));
				BigDecimal max = product.price().multiply(BigDecimal.valueOf(1.3));
				updated = updated.withPriceRange(min, max);
			}
			if (product.embedding() != null) {
				updated = updated.withEmbedding(product.embedding());
			}
			userPreferencePort.save(updated);
		});
	}

	private static List<Product> mergeByRrf(List<Product> vectorHits, List<Product> keywordHits, int topK) {
		List<Long> vectorIds = vectorHits.stream().map(Product::id).toList();
		List<Long> keywordIds = keywordHits.stream().map(Product::id).toList();
		List<RrfMerger.ScoredId> merged = RrfMerger.merge(List.of(vectorIds, keywordIds), topK);
		Map<Long, Product> byId = new LinkedHashMap<>();
		vectorHits.forEach(p -> byId.put(p.id(), p));
		keywordHits.forEach(p -> byId.putIfAbsent(p.id(), p));
		return merged.stream().map(scored -> byId.get(scored.id())).filter(Objects::nonNull).toList();
	}

	private static boolean matchesFilter(Product product, ProductSearchFilter filter) {
		if (filter.availableOnly() && !product.available()) {
			return false;
		}
		if (filter.category() != null && !equalsIgnore(product.category(), filter.category())) {
			return false;
		}
		if (filter.brand() != null && !equalsIgnore(product.brand(), filter.brand())) {
			return false;
		}
		if (filter.excludeBrand() != null && equalsIgnore(product.brand(), filter.excludeBrand())) {
			return false;
		}
		if (filter.color() != null && !equalsIgnore(product.color(), filter.color())) {
			return false;
		}
		if (filter.minPrice() != null && product.price().compareTo(filter.minPrice()) < 0) {
			return false;
		}
		if (filter.maxPrice() != null && product.price().compareTo(filter.maxPrice()) > 0) {
			return false;
		}
		return true;
	}

	private static boolean equalsIgnore(String left, String right) {
		return left != null && right != null && left.trim().equalsIgnoreCase(right.trim());
	}

	private static double keywordOverlap(String query, Product product) {
		if (query == null || query.isBlank()) {
			return 0;
		}
		String q = query.toLowerCase(Locale.ROOT);
		double score = 0;
		if (product.name() != null && q.contains(product.name().toLowerCase(Locale.ROOT))) {
			score += 0.6;
		}
		for (String keyword : product.keywords()) {
			if (keyword != null && q.contains(keyword.toLowerCase(Locale.ROOT))) {
				score += 0.15;
			}
		}
		return Math.min(1.0, score);
	}

	private static double cosine(float[] a, float[] b) {
		int n = Math.min(a.length, b.length);
		double dot = 0;
		double na = 0;
		double nb = 0;
		for (int i = 0; i < n; i++) {
			dot += a[i] * b[i];
			na += a[i] * a[i];
			nb += b[i] * b[i];
		}
		if (na == 0 || nb == 0) {
			return 0;
		}
		return dot / (Math.sqrt(na) * Math.sqrt(nb));
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
