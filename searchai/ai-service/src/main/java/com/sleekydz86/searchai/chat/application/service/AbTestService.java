package com.sleekydz86.searchai.chat.application.service;

import com.sleekydz86.searchai.chat.application.port.out.LlmStreamPort;
import com.sleekydz86.searchai.router.application.service.ModelCatalogService;
import com.sleekydz86.searchai.router.domain.ModelTier;
import com.sleekydz86.searchai.usage.domain.TokenCostCalculator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public final class AbTestService {

	private final LlmStreamPort llmStreamPort;
	private final ModelCatalogService modelCatalogService;

	public AbTestService(LlmStreamPort llmStreamPort, ModelCatalogService modelCatalogService) {
		this.llmStreamPort = llmStreamPort;
		this.modelCatalogService = modelCatalogService;
	}

	public Mono<AbTestResult> compare(String prompt, List<String> tiers) {
		List<String> selected = (tiers == null || tiers.isEmpty())
			? List.of("SOL", "ASTRA")
			: tiers;
		List<Mono<Variant>> monos = selected.stream()
			.map(tierName -> runVariant(prompt, ModelTier.valueOf(tierName.trim().toUpperCase())))
			.toList();
		return Mono.zip(monos, arr -> {
			List<Variant> variants = new ArrayList<>();
			for (Object item : arr) {
				variants.add((Variant) item);
			}
			return new AbTestResult(prompt, variants);
		});
	}

	private Mono<Variant> runVariant(String prompt, ModelTier tier) {
		String model = modelCatalogService.modelName(tier);
		long started = System.currentTimeMillis();
		return llmStreamPort.stream(prompt, model)
			.collectList()
			.map(parts -> {
				String answer = String.join("", parts);
				long promptTokens = TokenCostCalculator.estimatePromptTokens(prompt);
				long completionTokens = TokenCostCalculator.estimatePromptTokens(answer);
				long latency = System.currentTimeMillis() - started;
				double cost = TokenCostCalculator.estimate(model, promptTokens, completionTokens);
				return new Variant(tier.name(), model, answer, latency, promptTokens, completionTokens,
					promptTokens + completionTokens, cost, true, null);
			})
			.onErrorResume(error -> Mono.just(new Variant(
				tier.name(), model, "", System.currentTimeMillis() - started, 0, 0, 0, 0, false, error.getMessage()
			)));
	}

	public record Variant(
		String tier,
		String model,
		String response,
		long latencyMs,
		long promptTokens,
		long completionTokens,
		long totalTokens,
		double estimatedCost,
		boolean success,
		String error
	) {
	}

	public record AbTestResult(String prompt, List<Variant> variants) {
	}
}
