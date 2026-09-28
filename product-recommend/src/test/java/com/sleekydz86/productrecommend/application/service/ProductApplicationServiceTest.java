package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.application.port.out.EmbeddingPort;
import com.sleekydz86.productrecommend.application.port.out.ProductRepositoryPort;
import com.sleekydz86.productrecommend.domain.product.EmbeddingStatus;
import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.product.ProductEmbeddingRequested;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
	private ProductEmbeddingListener embeddingListener;
	private FakeRepo repo;

	@BeforeEach
	void setUp() {
		repo = new FakeRepo();
		EmbeddingPort embedding = new EmbeddingPort() {
			@Override
			public int dimensions() {
				return 8;
			}

			@Override
			public float[] embed(String text) {
				float[] v = new float[8];
				String lower = text.toLowerCase();
				if (lower.contains("gaming") || lower.contains("게임") || lower.contains("그래픽")) {
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
		service = new ProductApplicationService(repo, embedding, event -> {
			if (event instanceof ProductEmbeddingRequested requested) {
				embeddingListener.embedProduct(requested.productId());
			}
		});
	}

	@Test
	void createsAndFindsSimilarProducts() {
		Product created = service.create("게이밍 노트북", List.of("gaming", "high-performance", "graphics-card"));
		service.create("비즈니스 노트북", List.of("office", "document", "lightweight"));
		service.create("게이밍 마우스", List.of("gaming", "high-sensitivity", "RGB"));

		Product gamingLaptop = service.findById(created.id()).orElseThrow();
		assertThat(gamingLaptop.embeddingStatus()).isEqualTo(EmbeddingStatus.READY);

		List<Product> results = service.knnSearch(List.of("gaming computer"), 3);
		assertThat(results).isNotEmpty();
		assertThat(results.getFirst().name()).contains("게이밍");

		Long gamingId = gamingLaptop.id();
		List<Product> similar = service.findSimilarProducts(gamingId, 2);
		assertThat(similar).noneMatch(p -> p.id().equals(gamingId));
	}

	@Test
	void hybridSearchMergesVectorAndKeywordHits() {
		service.create("MacBook Pro 16", List.of("laptop", "apple", "developer"));
		service.create("게이밍 노트북", List.of("gaming", "laptop"));

		List<Product> hits = service.hybridSearch(List.of("MacBook Pro 16"), 5);
		assertThat(hits).isNotEmpty();
		assertThat(hits.getFirst().name()).containsIgnoringCase("MacBook");
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
			return store.values().stream()
				.filter(p -> p.embedding() != null)
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
