package com.sleekydz86.searchai.usage.domain;

import com.sleekydz86.searchai.router.application.service.ModelCatalogService;
import com.sleekydz86.searchai.router.domain.ModelTier;
import org.springframework.stereotype.Component;

@Component
public final class TokenCostCalculator {

	private static volatile ModelCatalogService catalog;

	public TokenCostCalculator(ModelCatalogService modelCatalogService) {
		catalog = modelCatalogService;
	}

	public static double estimate(String modelId, long promptTokens, long completionTokens) {
		if (catalog != null) {
			return catalog.estimateCost(modelId, promptTokens, completionTokens);
		}
		ModelTier tier = resolveTier(modelId);
		double inputPer1k = switch (tier) {
			case LUNA -> 0.0002;
			case TERRA -> 0.0005;
			case SOL -> 0.002;
			case ASTRA -> 0.008;
		};
		return (promptTokens / 1000.0) * inputPer1k + (completionTokens / 1000.0) * inputPer1k * 3;
	}

	public static long estimatePromptTokens(String text) {
		if (text == null || text.isBlank()) {
			return 1L;
		}
		return Math.max(1L, Math.round(text.length() / 3.5));
	}

	public static ModelTier resolveTier(String modelId) {
		if (catalog != null) {
			return catalog.resolveTier(modelId);
		}
		if (modelId == null) {
			return ModelTier.TERRA;
		}
		String lower = modelId.toLowerCase();
		if (lower.contains("astra")) return ModelTier.ASTRA;
		if (lower.contains("sol")) return ModelTier.SOL;
		if (lower.contains("terra")) return ModelTier.TERRA;
		if (lower.contains("luna")) return ModelTier.LUNA;
		return ModelTier.TERRA;
	}
}
