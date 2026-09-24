package com.sleekydz86.searchai.chat.application.service;

import com.sleekydz86.searchai.chat.application.port.in.StreamChatUseCase;
import com.sleekydz86.searchai.chat.application.port.out.LlmStreamPort;
import com.sleekydz86.searchai.chat.application.port.out.ResponseCachePort;
import com.sleekydz86.searchai.chat.application.strategy.ChatPromptStrategyFactory;
import com.sleekydz86.searchai.chat.domain.ChatQuery;
import com.sleekydz86.searchai.chat.domain.StreamChunk;
import com.sleekydz86.searchai.global.metrics.LlmMetricsService;
import com.sleekydz86.searchai.router.application.service.ModelRouterService;
import com.sleekydz86.searchai.router.domain.RoutingDecision;
import com.sleekydz86.searchai.usage.application.service.RequestClassifierService;
import com.sleekydz86.searchai.usage.application.service.UsagePolicyService;
import com.sleekydz86.searchai.usage.domain.PolicyDecision;
import com.sleekydz86.searchai.usage.domain.RequestType;
import com.sleekydz86.searchai.usage.domain.TokenCostCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public final class StreamChatService implements StreamChatUseCase {

	private static final Logger log = LoggerFactory.getLogger(StreamChatService.class);

	private final ChatPromptStrategyFactory strategyFactory;
	private final LlmStreamPort llmStreamPort;
	private final ModelRouterService modelRouterService;
	private final UsagePolicyService usagePolicyService;
	private final ResponseCachePort responseCachePort;
	private final LlmMetricsService llmMetricsService;
	private final RequestClassifierService requestClassifierService;

	public StreamChatService(
		ChatPromptStrategyFactory strategyFactory,
		LlmStreamPort llmStreamPort,
		ModelRouterService modelRouterService,
		UsagePolicyService usagePolicyService,
		ResponseCachePort responseCachePort,
		LlmMetricsService llmMetricsService,
		RequestClassifierService requestClassifierService
	) {
		this.strategyFactory = strategyFactory;
		this.llmStreamPort = llmStreamPort;
		this.modelRouterService = modelRouterService;
		this.usagePolicyService = usagePolicyService;
		this.responseCachePort = responseCachePort;
		this.llmMetricsService = llmMetricsService;
		this.requestClassifierService = requestClassifierService;
	}

	@Override
	public Flux<StreamChunk> streamChat(ChatQuery query) {
		String userId = query.userId();
		RequestType requestType = requestClassifierService.classify(query.mode(), query.message());
		long estimated = TokenCostCalculator.estimatePromptTokens(query.message());
		log.info("【사용자: {}】모드={} 요청유형={} 예상토큰={}", userId, query.mode(), requestType, estimated);

		return Mono.fromCallable(() -> usagePolicyService.authorize(query.username(), requestType, estimated))
			.subscribeOn(Schedulers.boundedElastic())
			.flatMapMany(policy -> {
				if (!policy.allowed()) {
					return Flux.just(StreamChunk.add(policy.message()), StreamChunk.finish("done"));
				}
				if (!policy.llmRequired() || requestType.isInternalOnly()) {
					return internalAnswer(query, requestType, policy);
				}
				if (policy.generalBlocked() && requestType.requiresExternalLlm()) {
					return Flux.just(StreamChunk.add(policy.message()), StreamChunk.finish("done"));
				}
				return continueChat(query, requestType, policy);
			})
			.onErrorResume(error -> {
				log.error("【사용자: {}】AI 스트림 오류: {}", userId, error.getMessage(), error);
				return Flux.just(
					StreamChunk.add("죄송합니다, 서비스에 문제가 발생했습니다. 잠시 후 다시 시도해 주세요."),
					StreamChunk.finish("done")
				);
			});
	}

	private Flux<StreamChunk> internalAnswer(ChatQuery query, RequestType requestType, PolicyDecision policy) {
		UsagePolicyService.UsageSnapshot snapshot = usagePolicyService.snapshot(query.username());
		String body = switch (requestType) {
			case FAQ -> "FAQ: PDF 업로드·지식베이스·인터넷 검색은 관리자 전용입니다. 일반 사용자는 직접 대화만 가능합니다.";
			case DB -> String.format(
				"DB 조회 결과 — 오늘 %d / %d tokens (budget %s), 이번 달 %d tokens, 오늘 비용 $%.4f",
				snapshot.todayTokens(), snapshot.dailyLimit(), snapshot.budgetState(),
				snapshot.monthTokens(), snapshot.todayCost()
			);
			default -> policy.message().isBlank()
				? "내부 데이터로 처리했습니다."
				: policy.message();
		};
		return Flux.just(StreamChunk.add(body), StreamChunk.finish("done"));
	}

	private Flux<StreamChunk> continueChat(ChatQuery query, RequestType requestType, PolicyDecision policy) {
		return Mono.fromCallable(() -> resolveRouting(query, policy))
			.subscribeOn(Schedulers.boundedElastic())
			.flatMapMany(decision -> strategyFactory.resolve(query.mode())
				.buildPrompt(query.message())
				.flatMapMany(prompt -> servePrompt(query, requestType, decision, prompt, policy)));
	}

	private RoutingDecision resolveRouting(ChatQuery query, PolicyDecision policy) {
		RoutingDecision raw = query.isManualRouting() && query.forcedTier() != null && !query.forcedTier().isBlank()
			? modelRouterService.routeManual(query.forcedTier())
			: modelRouterService.route(query.message());
		return modelRouterService.applyMaxTier(raw, policy.maxAllowedTier());
	}

	private Flux<StreamChunk> servePrompt(
		ChatQuery query,
		RequestType requestType,
		RoutingDecision decision,
		String prompt,
		PolicyDecision policy
	) {
		String cacheKey = sha256(decision.model() + "|stream|maxOut=" + policy.maxOutputTokens() + "|" + prompt);
		var cached = responseCachePort.get(cacheKey);
		if (cached.isPresent()) {
			llmMetricsService.recordCache(true);
			usagePolicyService.record(
				query.username(), decision, requestType,
				TokenCostCalculator.estimatePromptTokens(prompt),
				TokenCostCalculator.estimatePromptTokens(cached.get()),
				1L, true, true, decision.model(), null
			);
			return Flux.concat(
				Mono.just(StreamChunk.routing(decision.asSsePayload(Map.of(
					"cached", "true",
					"budget", policy.budgetState().name(),
					"requestType", requestType.name()
				)))),
				Flux.fromIterable(chunkText(trimToMaxOutput(cached.get(), policy.maxOutputTokens()), 28)).map(StreamChunk::add),
				Mono.just(StreamChunk.finish("done"))
			);
		}

		AtomicReference<StringBuilder> buffer = new AtomicReference<>(new StringBuilder());
		AtomicLong started = new AtomicLong(System.currentTimeMillis());
		return Flux.concat(
			Mono.just(StreamChunk.routing(decision.asSsePayload(Map.of(
				"cached", "false",
				"budget", policy.budgetState().name(),
				"requestType", requestType.name()
			)))),
			llmStreamPort.stream(prompt, decision.model())
				.doOnNext(part -> buffer.get().append(part))
				.map(StreamChunk::add)
				.doOnComplete(() -> {
					String answer = trimToMaxOutput(buffer.get().toString(), policy.maxOutputTokens());
					responseCachePort.put(cacheKey, answer);
					usagePolicyService.record(
						query.username(), decision, requestType,
						TokenCostCalculator.estimatePromptTokens(prompt),
						TokenCostCalculator.estimatePromptTokens(answer),
						System.currentTimeMillis() - started.get(),
						true, false, decision.model(), null
					);
					llmMetricsService.recordTokensTotal(
						TokenCostCalculator.estimatePromptTokens(prompt) + TokenCostCalculator.estimatePromptTokens(answer)
					);
				})
				.doOnError(error -> usagePolicyService.record(
					query.username(), decision, requestType,
					TokenCostCalculator.estimatePromptTokens(prompt),
					0,
					System.currentTimeMillis() - started.get(),
					false, false, decision.model(), error.getClass().getSimpleName()
				)),
			Mono.just(StreamChunk.finish("done"))
		);
	}

	private static String trimToMaxOutput(String text, long maxOutputTokens) {
		if (text == null || maxOutputTokens <= 0) {
			return text;
		}
		int maxChars = (int) Math.min(Integer.MAX_VALUE, maxOutputTokens * 4);
		if (text.length() <= maxChars) {
			return text;
		}
		return text.substring(0, maxChars) + "…";
	}

	private static java.util.List<String> chunkText(String text, int size) {
		java.util.List<String> parts = new java.util.ArrayList<>();
		for (int i = 0; i < text.length(); i += size) {
			parts.add(text.substring(i, Math.min(text.length(), i + size)));
		}
		return parts;
	}

	private static String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception ex) {
			return Integer.toHexString(value.hashCode());
		}
	}
}
