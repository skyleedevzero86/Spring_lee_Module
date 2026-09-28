package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.application.port.out.BehaviorEventPort;
import com.sleekydz86.productrecommend.application.port.out.EmbeddingPort;
import com.sleekydz86.productrecommend.application.port.out.OutboxPort;
import com.sleekydz86.productrecommend.application.port.out.ProductRepositoryPort;
import com.sleekydz86.productrecommend.application.port.out.UserPreferencePort;
import com.sleekydz86.productrecommend.domain.outbox.OutboxMessage;
import com.sleekydz86.productrecommend.domain.outbox.OutboxStatus;
import com.sleekydz86.productrecommend.domain.product.EmbeddingStatus;
import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.product.ProductStatus;
import com.sleekydz86.productrecommend.domain.recommend.ProductSearchFilter;
import com.sleekydz86.productrecommend.domain.recommend.RankedRecommendation;
import com.sleekydz86.productrecommend.domain.user.BehaviorEvent;
import com.sleekydz86.productrecommend.domain.user.BehaviorEventType;
import com.sleekydz86.productrecommend.domain.user.UserPreference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class ProductApplicationServiceTest {

	private ProductApplicationService service;
	private FakeRepo repo;
	private FakeOutbox outbox;
	private ProductEmbeddingListener embeddingListener;

	@BeforeEach
	void setUp() {
		repo = new FakeRepo();
		outbox = new FakeOutbox();
		EmbeddingPort embedding = new EmbeddingPort() {
			@Override
			public int dimensions() {
				return 8;
			}

			@Override
			public float[] embed(String text) {
				float[] v = new float[8];
				String lower = text.toLowerCase();
				if (lower.contains("gaming") || lower.contains("게임") || lower.contains("그래픽") || lower.contains("러닝")) {
					v[0] = 1;
				} else if (lower.contains("office") || lower.contains("사무") || lower.contains("비즈니스")) {
					v[1] = 1;
				} else {
					v[Math.abs(text.hashCode()) % 8] = 1;
				}
				return v;
			}

			@Override
			public String providerName() {
				return "test";
			}

			@Override
			public String modelName() {
				return "test-model";
			}
		};
		embeddingListener = new ProductEmbeddingListener(repo, embedding);
		service = new ProductApplicationService(
			repo,
			embedding,
			outbox,
			new FakePreference(),
			new FakeBehavior()
		);
	}

	@Test
	void createsAndRecommendsWithEvidence() {
		Product nike = service.create(
			"나이키 페가수스",
			List.of("러닝", "운동화"),
			"RUNNING_SHOES",
			"NIKE",
			"BLACK",
			BigDecimal.valueOf(139000),
			10
		);
		service.create(
			"아디다스 울트라부스트",
			List.of("러닝", "운동화"),
			"RUNNING_SHOES",
			"ADIDAS",
			"BLACK",
			BigDecimal.valueOf(99000),
			10
		);
		service.create(
			"비즈니스 노트북",
			List.of("office", "laptop"),
			"LAPTOP",
			"DELL",
			"BLACK",
			BigDecimal.valueOf(1500000),
			5
		);
		drainOutbox();

		List<RankedRecommendation> ranked = service.recommend(
			"u1",
			"10만원 이하 검정 러닝화 나이키 말고",
			5
		);
		assertThat(ranked).isNotEmpty();
		assertThat(ranked.getFirst().product().brand()).isNotEqualToIgnoringCase("NIKE");
		assertThat(ranked.getFirst().reasons()).isNotEmpty();
		assertThat(nike.embeddingStatus()).isEqualTo(EmbeddingStatus.PENDING);
	}

	private void drainOutbox() {
		for (OutboxMessage message : outbox.findByStatus(OutboxStatus.PENDING, 100)) {
			if ("PRODUCT_EMBEDDING_REQUESTED".equals(message.eventType())) {
				embeddingListener.embedProduct(Long.valueOf(message.aggregateId()));
			}
			outbox.save(message.markPublished());
		}
	}

	private static final class FakeOutbox implements OutboxPort {
		private final Map<String, OutboxMessage> store = new ConcurrentHashMap<>();

		@Override
		public OutboxMessage save(OutboxMessage message) {
			store.put(message.id(), message);
			return message;
		}

		@Override
		public List<OutboxMessage> findByStatus(OutboxStatus status, int limit) {
			return store.values().stream().filter(m -> m.status() == status).limit(limit).toList();
		}
	}

	private static final class FakePreference implements UserPreferencePort {
		private final Map<String, UserPreference> store = new ConcurrentHashMap<>();

		@Override
		public Optional<UserPreference> findByUserId(String userId) {
			return Optional.ofNullable(store.get(userId));
		}

		@Override
		public UserPreference save(UserPreference preference) {
			store.put(preference.userId(), preference);
			return preference;
		}
	}

	private static final class FakeBehavior implements BehaviorEventPort {
		private final List<BehaviorEvent> events = new ArrayList<>();

		@Override
		public BehaviorEvent save(BehaviorEvent event) {
			events.add(event);
			return event;
		}

		@Override
		public List<BehaviorEvent> findByUserId(String userId, int limit) {
			return events.stream().filter(e -> e.userId().equals(userId)).limit(limit).toList();
		}

		@Override
		public long countByEventType(String eventType) {
			return events.stream().filter(e -> e.eventType().name().equals(eventType)).count();
		}

		@Override
		public long countRecommendationClicks() {
			return countByEventType(BehaviorEventType.RECOMMENDATION_CLICK.name());
		}

		@Override
		public long countRecommendationImpressions() {
			return countByEventType(BehaviorEventType.RECOMMENDATION_IMPRESSION.name());
		}
	}

	private static final class FakeRepo implements ProductRepositoryPort {
		private final Map<Long, Product> store = new ConcurrentHashMap<>();
		private final AtomicLong seq = new AtomicLong(1);

		@Override
		public Product save(Product product) {
			Long id = product.id() == null ? seq.getAndIncrement() : product.id();
			Product saved = Product.rehydrate(
				id,
				product.name(),
				product.keywords(),
				product.category(),
				product.brand(),
				product.color(),
				product.price(),
				product.stock(),
				product.status() == null ? ProductStatus.ACTIVE : product.status(),
				product.popularityScore(),
				product.createdAt() == null ? Instant.now() : product.createdAt(),
				product.embedding(),
				product.embeddingStatus(),
				product.embeddingModel()
			);
			store.put(id, saved);
			return saved;
		}

		@Override
		public Optional<Product> findById(Long id) {
			return Optional.ofNullable(store.get(id));
		}

		@Override
		public List<Product> findAll() {
			return new ArrayList<>(store.values());
		}

		@Override
		public void deleteById(Long id) {
			store.remove(id);
		}

		@Override
		public List<Product> searchByEmbeddingNear(float[] queryVector, int limit) {
			return searchByEmbeddingNearFiltered(queryVector, new ProductSearchFilter(null, null, null, null, null, null, false), limit);
		}

		@Override
		public List<Product> searchByEmbeddingNearFiltered(float[] queryVector, ProductSearchFilter filter, int limit) {
			return store.values().stream()
				.filter(p -> p.embedding() != null)
				.filter(p -> filter.excludeBrand() == null || p.brand() == null || !p.brand().equalsIgnoreCase(filter.excludeBrand()))
				.filter(p -> filter.maxPrice() == null || p.price().compareTo(filter.maxPrice()) <= 0)
				.filter(p -> filter.category() == null || filter.category().equalsIgnoreCase(p.category()))
				.filter(p -> !filter.availableOnly() || p.available())
				.map(p -> Map.entry(p, dot(queryVector, p.embedding())))
				.sorted(Comparator.comparingDouble((Map.Entry<Product, Double> e) -> e.getValue()).reversed())
				.limit(limit)
				.map(Map.Entry::getKey)
				.toList();
		}

		@Override
		public List<Product> searchByNameTrigram(String query, int limit) {
			String needle = query.toLowerCase();
			return store.values().stream()
				.filter(p -> p.name().toLowerCase().contains(needle) || needle.contains(p.name().toLowerCase()))
				.limit(limit)
				.toList();
		}

		@Override
		public List<Product> findByEmbeddingStatus(EmbeddingStatus status) {
			return store.values().stream().filter(p -> p.embeddingStatus() == status).toList();
		}

		@Override
		public List<Product> findByEmbeddingIsNull() {
			return store.values().stream().filter(p -> p.embedding() == null).toList();
		}

		@Override
		public List<Product> findByEmbeddingModelNot(String model) {
			return store.values().stream()
				.filter(p -> p.embeddingModel() == null || !model.equals(p.embeddingModel()))
				.toList();
		}

		@Override
		public List<Product> findPopular(int limit) {
			return store.values().stream()
				.sorted(Comparator.comparingDouble(Product::popularityScore).reversed())
				.limit(limit)
				.toList();
		}

		private static double dot(float[] a, float[] b) {
			double sum = 0;
			int n = Math.min(a.length, b.length);
			for (int i = 0; i < n; i++) {
				sum += a[i] * b[i];
			}
			return sum;
		}
	}
}
