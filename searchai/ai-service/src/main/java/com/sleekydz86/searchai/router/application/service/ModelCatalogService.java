package com.sleekydz86.searchai.router.application.service;

import com.sleekydz86.searchai.global.config.AppProperties;
import com.sleekydz86.searchai.router.domain.ModelTier;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

@Service
public final class ModelCatalogService {

	private static final Logger log = LoggerFactory.getLogger(ModelCatalogService.class);

	private final AppProperties appProperties;
	private final Map<ModelTier, AppProperties.ModelDef> catalog = new EnumMap<>(ModelTier.class);

	public ModelCatalogService(AppProperties appProperties) {
		this.appProperties = appProperties;
	}

	@PostConstruct
	public void load() {
		Map<String, AppProperties.ModelDef> configured = appProperties.models() == null
			? Map.of()
			: appProperties.models().tiersOrEmpty();
		Map<String, AppProperties.ModelDef> defaults = AppProperties.defaultModels();
		for (ModelTier tier : ModelTier.values()) {
			AppProperties.ModelDef def = configured.getOrDefault(tier.name(), defaults.get(tier.name()));
			catalog.put(tier, def);
			log.info("모델 카탈로그 로드: {} → {} (enabled={}, in=${}/1k, out=${}/1k)",
				tier, def.modelName(), def.enabled(), def.estimatedInputCost(), def.estimatedOutputCost());
		}
	}

	public AppProperties.ModelDef definition(ModelTier tier) {
		return catalog.getOrDefault(tier, AppProperties.defaultModels().get(tier.name()));
	}

	public String modelName(ModelTier tier) {
		return definition(tier).modelName();
	}

	public boolean enabled(ModelTier tier) {
		return definition(tier).enabled();
	}

	public double inputCostPer1k(ModelTier tier) {
		return definition(tier).estimatedInputCost();
	}

	public double outputCostPer1k(ModelTier tier) {
		return definition(tier).estimatedOutputCost();
	}

	public ModelTier resolveTier(String modelId) {
		if (modelId == null || modelId.isBlank()) {
			return ModelTier.TERRA;
		}
		for (Map.Entry<ModelTier, AppProperties.ModelDef> entry : catalog.entrySet()) {
			if (modelId.equalsIgnoreCase(entry.getValue().modelName())) {
				return entry.getKey();
			}
		}
		String lower = modelId.toLowerCase(Locale.ROOT);
		if (lower.contains("astra")) return ModelTier.ASTRA;
		if (lower.contains("sol")) return ModelTier.SOL;
		if (lower.contains("terra")) return ModelTier.TERRA;
		if (lower.contains("luna")) return ModelTier.LUNA;
		return ModelTier.TERRA;
	}

	public double estimateCost(String modelId, long promptTokens, long completionTokens) {
		ModelTier tier = resolveTier(modelId);
		return (promptTokens / 1000.0) * inputCostPer1k(tier)
			+ (completionTokens / 1000.0) * outputCostPer1k(tier);
	}
}
