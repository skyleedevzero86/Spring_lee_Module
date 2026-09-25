package com.sleekydz86.searchai.router.application.policy;

import com.sleekydz86.searchai.global.config.AppProperties;
import com.sleekydz86.searchai.router.application.service.ModelCatalogService;
import com.sleekydz86.searchai.router.domain.ModelTier;
import com.sleekydz86.searchai.router.domain.RoutingDecision;
import com.sleekydz86.searchai.usage.application.port.out.UsageStorePort;
import com.sleekydz86.searchai.usage.domain.LlmUsageRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public final class CostAwareRoutingPolicy {

	private static final Logger log = LoggerFactory.getLogger(CostAwareRoutingPolicy.class);

	private final AppProperties appProperties;
	private final UsageStorePort usageStorePort;
	private final ModelCatalogService modelCatalogService;

	public CostAwareRoutingPolicy(
		AppProperties appProperties,
		UsageStorePort usageStorePort,
		ModelCatalogService modelCatalogService
	) {
		this.appProperties = appProperties;
		this.usageStorePort = usageStorePort;
		this.modelCatalogService = modelCatalogService;
	}

	public RoutingDecision apply(RoutingDecision decision) {
		AppProperties.CostAware cfg = appProperties.costAware();
		if (cfg == null || !cfg.enabledOrDefault()) {
			return decision;
		}
		if (decision.tier() == ModelTier.LUNA) {
			return decision;
		}
		ModelTier cheaper = decision.tier().downgrade();
		String cheaperModel = modelCatalogService.modelName(cheaper);
		Instant from = Instant.now().minus(7, ChronoUnit.DAYS);
		List<LlmUsageRecord> recent = usageStorePort.findBetween(from, Instant.now()).stream()
			.filter(r -> cheaperModel.equals(r.model()))
			.toList();
		if (recent.size() < 5) {
			return decision;
		}
		double successRate = recent.stream().filter(LlmUsageRecord::success).count() * 1.0 / recent.size();
		if (successRate >= cfg.minSuccessRateOrDefault()) {
			log.info("비용 인식 라우팅: {} 성공률 {}% ≥ {}% → {} 우선",
				cheaper, String.format("%.1f", successRate * 100),
				String.format("%.0f", cfg.minSuccessRateOrDefault() * 100), cheaper);
			return decision.withTier(cheaper, cheaperModel);
		}
		return decision;
	}
}
