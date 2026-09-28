package com.sleekydz86.productrecommend.adapter.in.web;

import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import com.sleekydz86.productrecommend.application.service.ProductChatService;
import com.sleekydz86.productrecommend.application.service.StructuredChatResponse;
import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.product.ProductStatus;
import com.sleekydz86.productrecommend.domain.product.RecommendationResponse;
import com.sleekydz86.productrecommend.domain.recommend.RankedRecommendation;
import com.sleekydz86.productrecommend.domain.recommend.SearchCondition;
import com.sleekydz86.productrecommend.domain.user.BehaviorEvent;
import com.sleekydz86.productrecommend.domain.user.BehaviorEventType;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private static final String SESSION_CONVERSATION_KEY = "chat.conversationId";
	private static final String SESSION_USER_KEY = "recommend.userId";

	private final ProductUseCase productUseCase;
	private final ObjectProvider<ProductChatService> productChatService;

	public ProductController(
		ProductUseCase productUseCase,
		ObjectProvider<ProductChatService> productChatService
	) {
		this.productUseCase = productUseCase;
		this.productChatService = productChatService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@Valid @RequestBody CreateRequest request) {
		return ProductResponse.from(productUseCase.create(
			request.name(),
			request.keywords(),
			request.category(),
			request.brand(),
			request.color(),
			request.price(),
			request.stock() == null ? 10 : request.stock()
		));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> findById(@PathVariable Long id) {
		return productUseCase.findById(id)
			.map(ProductResponse::from)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@GetMapping
	public List<ProductResponse> findAll() {
		return productUseCase.findAll().stream().map(ProductResponse::from).toList();
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> update(
		@PathVariable Long id,
		@Valid @RequestBody UpdateRequest request
	) {
		try {
			return ResponseEntity.ok(ProductResponse.from(productUseCase.update(
				id,
				request.name(),
				request.keywords(),
				request.category(),
				request.brand(),
				request.color(),
				request.price(),
				request.stock() == null ? 0 : request.stock(),
				request.status()
			)));
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		productUseCase.delete(id);
	}

	@PostMapping("/search")
	public List<ProductResponse> knnSearch(@RequestBody SearchRequest request) {
		List<Product> hits = Boolean.TRUE.equals(request.hybrid())
			? productUseCase.hybridSearch(request.keywords(), request.k())
			: productUseCase.knnSearch(request.keywords(), request.k());
		return hits.stream().map(ProductResponse::from).toList();
	}

	@PostMapping("/recommend")
	public RecommendResponse recommend(
		@Valid @RequestBody RecommendRequest request,
		HttpSession session
	) {
		String userId = resolveUserId(request.userId(), session);
		SearchCondition condition = productUseCase.understandQuery(request.query(), request.limit());
		List<RankedRecommendation> ranked = productUseCase.recommend(userId, request.query(), request.limit());
		return new RecommendResponse(
			userId,
			condition,
			ranked.stream().map(RecommendationItemResponse::from).toList()
		);
	}

	@PostMapping("/events")
	@ResponseStatus(HttpStatus.CREATED)
	public EventResponse trackEvent(@Valid @RequestBody EventRequest request, HttpSession session) {
		String userId = resolveUserId(request.userId(), session);
		BehaviorEvent event = productUseCase.trackEvent(
			userId,
			request.productId(),
			request.eventType(),
			request.impressionId(),
			request.position(),
			request.query()
		);
		return new EventResponse(event.eventId(), event.eventType().name());
	}

	@GetMapping("/stats/recommendations")
	public ProductUseCase.RecommendationStats recommendationStats() {
		return productUseCase.stats();
	}

	@GetMapping("/{id}/similar")
	public ResponseEntity<List<ProductResponse>> findSimilar(
		@PathVariable Long id,
		@RequestParam(defaultValue = "5") int k
	) {
		try {
			return ResponseEntity.ok(
				productUseCase.findSimilarProducts(id, k).stream().map(ProductResponse::from).toList()
			);
		} catch (IllegalArgumentException ex) {
			return ResponseEntity.notFound().build();
		}
	}

	@PostMapping("/embeddings/reindex")
	public ReindexResponse reindex() {
		return new ReindexResponse(productUseCase.reindexMismatchedEmbeddings());
	}

	@PostMapping("/embeddings/retry")
	public ReindexResponse retryPending() {
		return new ReindexResponse(productUseCase.retryPendingEmbeddings());
	}

	@PostMapping("/chat")
	public ResponseEntity<ChatResponse> chat(
		@Valid @RequestBody ChatRequest request,
		@RequestHeader(value = "X-Conversation-Id", required = false) String conversationHeader,
		HttpSession session
	) {
		ProductChatService chatService = productChatService.getIfAvailable();
		if (chatService == null) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
				.body(new ChatResponse("챗봇 기능이 비활성화되어 있습니다", null, null));
		}
		String conversationId = resolveConversationId(conversationHeader, session);
		String userId = resolveUserId(null, session);
		StructuredChatResponse response = chatService.chat(request.message(), conversationId, userId);
		return ResponseEntity.ok(new ChatResponse(response.message(), response.recommendation(), conversationId));
	}

	@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter chatStream(
		@RequestParam String message,
		@RequestHeader(value = "X-Conversation-Id", required = false) String conversationHeader,
		HttpSession session
	) {
		ProductChatService chatService = productChatService.getIfAvailable();
		SseEmitter emitter = new SseEmitter(0L);
		if (chatService == null) {
			emitter.completeWithError(new IllegalStateException("챗봇 기능이 비활성화되어 있습니다"));
			return emitter;
		}
		String conversationId = resolveConversationId(conversationHeader, session);
		String userId = resolveUserId(null, session);
		Flux<String> response = chatService.chatStream(message, conversationId, userId);
		AtomicReference<Disposable> disposableRef = new AtomicReference<>();
		Runnable dispose = () -> {
			Disposable disposable = disposableRef.getAndSet(null);
			if (disposable != null && !disposable.isDisposed()) {
				disposable.dispose();
			}
		};
		emitter.onCompletion(dispose);
		emitter.onTimeout(dispose);
		emitter.onError(error -> dispose.run());
		Disposable disposable = response.subscribe(
			token -> {
				try {
					if (token != null) {
						emitter.send(SseEmitter.event().name("message").data(token));
					}
				} catch (IOException ex) {
					dispose.run();
					emitter.completeWithError(ex);
				}
			},
			error -> {
				dispose.run();
				emitter.completeWithError(error);
			},
			() -> {
				dispose.run();
				emitter.complete();
			}
		);
		disposableRef.set(disposable);
		return emitter;
	}

	private String resolveConversationId(String headerValue, HttpSession session) {
		if (headerValue != null && !headerValue.isBlank()) {
			String normalized = headerValue.trim();
			session.setAttribute(SESSION_CONVERSATION_KEY, normalized);
			return normalized;
		}
		Object existing = session.getAttribute(SESSION_CONVERSATION_KEY);
		if (existing instanceof String existingId && !existingId.isBlank()) {
			return existingId;
		}
		String generated = UUID.randomUUID().toString();
		session.setAttribute(SESSION_CONVERSATION_KEY, generated);
		return generated;
	}

	private String resolveUserId(String requestUserId, HttpSession session) {
		if (requestUserId != null && !requestUserId.isBlank()) {
			session.setAttribute(SESSION_USER_KEY, requestUserId.trim());
			return requestUserId.trim();
		}
		Object existing = session.getAttribute(SESSION_USER_KEY);
		if (existing instanceof String userId && !userId.isBlank()) {
			return userId;
		}
		String generated = "user-" + UUID.randomUUID();
		session.setAttribute(SESSION_USER_KEY, generated);
		return generated;
	}

	public record CreateRequest(
		@NotBlank String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		Integer stock
	) {
	}

	public record UpdateRequest(
		@NotBlank String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		Integer stock,
		ProductStatus status
	) {
	}

	public record SearchRequest(List<String> keywords, int k, Boolean hybrid) {
		public SearchRequest {
			if (k <= 0) {
				k = 10;
			}
		}
	}

	public record RecommendRequest(@NotBlank String query, String userId, int limit) {
		public RecommendRequest {
			if (limit <= 0) {
				limit = 5;
			}
		}
	}

	public record EventRequest(
		String userId,
		Long productId,
		@NotNull BehaviorEventType eventType,
		String impressionId,
		Integer position,
		String query
	) {
	}

	public record ChatRequest(@NotBlank String message) {
	}

	public record ChatResponse(String message, RecommendationResponse recommendation, String conversationId) {
	}

	public record ReindexResponse(int queued) {
	}

	public record EventResponse(String eventId, String eventType) {
	}

	public record RecommendResponse(String userId, SearchCondition condition, List<RecommendationItemResponse> items) {
	}

	public record RecommendationItemResponse(
		Long id,
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock,
		double score,
		List<String> reasons
	) {
		static RecommendationItemResponse from(RankedRecommendation ranked) {
			Product product = ranked.product();
			return new RecommendationItemResponse(
				product.id(),
				product.name(),
				product.keywords(),
				product.category(),
				product.brand(),
				product.color(),
				product.price(),
				product.stock(),
				ranked.score(),
				ranked.reasons()
			);
		}
	}

	public record ProductResponse(
		Long id,
		String name,
		List<String> keywords,
		String category,
		String brand,
		String color,
		BigDecimal price,
		int stock,
		String status,
		double popularityScore,
		String embeddingStatus,
		String embeddingModel
	) {
		static ProductResponse from(Product product) {
			return new ProductResponse(
				product.id(),
				product.name(),
				product.keywords(),
				product.category(),
				product.brand(),
				product.color(),
				product.price(),
				product.stock(),
				product.status() == null ? null : product.status().name(),
				product.popularityScore(),
				product.embeddingStatus() == null ? null : product.embeddingStatus().name(),
				product.embeddingModel()
			);
		}
	}
}
