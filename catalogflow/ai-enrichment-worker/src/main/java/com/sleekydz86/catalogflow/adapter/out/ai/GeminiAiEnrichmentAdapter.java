package com.sleekydz86.catalogflow.adapter.out.ai;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiAiEnrichmentAdapter implements AiEnrichmentPort {

	private final GoogleGenAiChatModel googleGenAiChatModel;
	private final ChatModelEnrichmentSupport chatModelEnrichmentSupport;

	public GeminiAiEnrichmentAdapter(
			GoogleGenAiChatModel googleGenAiChatModel,
			ChatModelEnrichmentSupport chatModelEnrichmentSupport) {
		this.googleGenAiChatModel = googleGenAiChatModel;
		this.chatModelEnrichmentSupport = chatModelEnrichmentSupport;
	}

	@Override
	public EnrichmentResult enrich(EnrichmentRequest request) {
		return chatModelEnrichmentSupport.enrich(googleGenAiChatModel, request);
	}
}
