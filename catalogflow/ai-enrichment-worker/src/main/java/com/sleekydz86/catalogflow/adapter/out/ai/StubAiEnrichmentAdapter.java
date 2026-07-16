package com.sleekydz86.catalogflow.adapter.out.ai;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "stub", matchIfMissing = true)
public class StubAiEnrichmentAdapter implements AiEnrichmentPort {

	private final StubEnrichmentEngine stubEnrichmentEngine;

	public StubAiEnrichmentAdapter(StubEnrichmentEngine stubEnrichmentEngine) {
		this.stubEnrichmentEngine = stubEnrichmentEngine;
	}

	@Override
	public EnrichmentResult enrich(EnrichmentRequest request) {
		return stubEnrichmentEngine.enrich(request);
	}
}
