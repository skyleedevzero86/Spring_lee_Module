package com.sleekydz86.searchai.router.application.policy;

import com.sleekydz86.searchai.router.application.service.ModelCatalogService;
import com.sleekydz86.searchai.router.domain.ModelTier;
import com.sleekydz86.searchai.router.domain.RoutingDecision;
import com.sleekydz86.searchai.usage.application.service.RuntimePolicyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public final class ConfidencePromotionPolicy {

	private static final Logger log = LoggerFactory.getLogger(ConfidencePromotionPolicy.class);

	private final RuntimePolicyService runtimePolicyService;
	private final ModelCatalogService modelCatalogService;

	public ConfidencePromotionPolicy(RuntimePolicyService runtimePolicyService, ModelCatalogService modelCatalogService) {
		this.runtimePolicyService = runtimePolicyService;
		this.modelCatalogService = modelCatalogService;
	}

	public RoutingDecision apply(RoutingDecision decision) {
		double threshold = runtimePolicyService.current().confidenceThreshold();
		if (decision.confidence() >= threshold) {
			return decision;
		}
		ModelTier upgraded = decision.tier().upgrade();
		if (upgraded == decision.tier()) {
			return decision;
		}
		log.info("Confidence {} < {} → {} 승격", decision.confidence(), threshold, upgraded);
		return decision.withTier(upgraded, modelCatalogService.modelName(upgraded));
	}
}
