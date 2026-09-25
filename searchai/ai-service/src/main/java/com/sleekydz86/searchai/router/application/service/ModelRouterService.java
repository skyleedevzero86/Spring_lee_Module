package com.sleekydz86.searchai.router.application.service;

import com.sleekydz86.searchai.router.adapter.out.rule.RuleBasedRouter;
import com.sleekydz86.searchai.router.application.policy.ConfidencePromotionPolicy;
import com.sleekydz86.searchai.router.application.policy.CostAwareRoutingPolicy;
import com.sleekydz86.searchai.router.application.port.out.ModelRouterPort;
import com.sleekydz86.searchai.router.domain.ModelTier;
import com.sleekydz86.searchai.router.domain.RoutingDecision;
import com.sleekydz86.searchai.router.domain.RoutingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public final class ModelRouterService {

	private static final Logger log = LoggerFactory.getLogger(ModelRouterService.class);

	private final RuleBasedRouter ruleBasedRouter;
	private final ModelRouterPort modelRouterPort;
	private final ModelCatalogService modelCatalogService;
	private final ModelHealthRegistry modelHealthRegistry;
	private final ConfidencePromotionPolicy confidencePromotionPolicy;
	private final CostAwareRoutingPolicy costAwareRoutingPolicy;

	public ModelRouterService(
		RuleBasedRouter ruleBasedRouter,
		ModelRouterPort modelRouterPort,
		ModelCatalogService modelCatalogService,
		ModelHealthRegistry modelHealthRegistry,
		ConfidencePromotionPolicy confidencePromotionPolicy,
		CostAwareRoutingPolicy costAwareRoutingPolicy
	) {
		this.ruleBasedRouter = ruleBasedRouter;
		this.modelRouterPort = modelRouterPort;
		this.modelCatalogService = modelCatalogService;
		this.modelHealthRegistry = modelHealthRegistry;
		this.confidencePromotionPolicy = confidencePromotionPolicy;
		this.costAwareRoutingPolicy = costAwareRoutingPolicy;
	}

	public RoutingDecision route(String prompt) {
		RoutingDecision decision = ruleBasedRouter.tryRoute(prompt)
			.map(d -> {
				log.info("규칙 라우터 선택 → 모델={} (신뢰도={})", d.model(), d.confidence());
				return d;
			})
			.orElseGet(() -> {
				RoutingDecision jev = modelRouterPort.route(prompt);
				RoutingDecision typed = new RoutingDecision(
					jev.tier(),
					modelCatalogService.modelName(jev.tier()),
					jev.confidence(),
					jev.probabilities(),
					RoutingType.JEV,
					jev.requestId()
				);
				log.info("Jev 라우터 선택 → 모델={} (신뢰도={})", typed.model(), typed.confidence());
				return confidencePromotionPolicy.apply(typed);
			});
		decision = costAwareRoutingPolicy.apply(decision);
		return applyHealth(decision);
	}

	public RoutingDecision routeManual(String forcedTier) {
		if (forcedTier == null || forcedTier.isBlank()) {
			throw new IllegalArgumentException("수동 라우팅 티어가 비어 있습니다");
		}
		ModelTier tier;
		try {
			tier = ModelTier.valueOf(forcedTier.trim().toUpperCase());
		} catch (IllegalArgumentException ex) {
			log.warn("잘못된 수동 라우팅 티어: {}", forcedTier);
			throw new IllegalArgumentException("지원하지 않는 모델 티어입니다: " + forcedTier);
		}
		log.info("수동 라우팅 지정 → {}", modelCatalogService.modelName(tier));
		return applyHealth(RoutingDecision.of(
			tier,
			modelCatalogService.modelName(tier),
			1.0,
			Map.of(tier.name(), 1.0),
			RoutingType.MANUAL
		));
	}

	public RoutingDecision applyMaxTier(RoutingDecision decision, ModelTier maxAllowed) {
		if (decision.tier().ordinal() <= maxAllowed.ordinal()) {
			return decision;
		}
		log.info("예산 정책 강등: {} → {}", decision.tier(), maxAllowed);
		return decision.withTier(maxAllowed, modelCatalogService.modelName(maxAllowed));
	}

	private RoutingDecision applyHealth(RoutingDecision decision) {
		if (!modelCatalogService.enabled(decision.tier()) || !modelHealthRegistry.isRoutable(decision.tier())) {
			ModelTier healthy = modelHealthRegistry.nearestAvailable(decision.tier());
			if (healthy != decision.tier() && modelCatalogService.enabled(healthy)) {
				log.info("상태 정책 우회: {} ({}) → {}", decision.tier(),
					modelHealthRegistry.health(decision.tier()), healthy);
				return decision.withTier(healthy, modelCatalogService.modelName(healthy));
			}
		}
		return decision;
	}
}
