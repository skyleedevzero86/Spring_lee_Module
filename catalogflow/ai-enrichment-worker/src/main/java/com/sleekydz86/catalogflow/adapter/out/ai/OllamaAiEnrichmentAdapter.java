package com.sleekydz86.catalogflow.adapter.out.ai;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "ollama")
public class OllamaAiEnrichmentAdapter implements AiEnrichmentPort {

	private final OllamaChatModel ollamaChatModel;
	private final ChatModelEnrichmentSupport chatModelEnrichmentSupport;

	public OllamaAiEnrichmentAdapter(
			OllamaChatModel ollamaChatModel,
			ChatModelEnrichmentSupport chatModelEnrichmentSupport) {
		this.ollamaChatModel = ollamaChatModel;
		this.chatModelEnrichmentSupport = chatModelEnrichmentSupport;
	}

	@Override
	public EnrichmentResult enrich(EnrichmentRequest request) {
		return chatModelEnrichmentSupport.enrich(ollamaChatModel, request);
	}
}
