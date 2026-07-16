package com.sleekydz86.catalogflow.adapter.out.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.global.config.AiProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

class ChatModelEnrichmentSupportTest {

	private ChatModelEnrichmentSupport support;

	@BeforeEach
	void setUp() {
		AiProperties properties = new AiProperties();
		properties.setModelName("ollama-llama3.2");
		properties.setPromptVersion("llm-prompt-v1");
		support = new ChatModelEnrichmentSupport(
				new EnrichmentPromptTemplate(),
				new EnrichmentResultValidator(),
				properties);
	}

	@Test
	void shouldParseChatModelJsonResponse() {
		ChatModel chatModel = mock(ChatModel.class);
		String json = """
				{"summary":"요약","generatedDescription":"상세","keywords":["무선","키보드"],"tags":["전자"],"recommendedCategory":"입력장치","warnings":"","requiresHumanReview":false,"confidence":0.91}
				""";
		when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(
				new Generation(new AssistantMessage(json)))));

		AiEnrichmentPort.EnrichmentResult result = support.enrich(
				chatModel,
				new AiEnrichmentPort.EnrichmentRequest(
						UUID.randomUUID(),
						"무선 키보드",
						"저소음",
						UUID.randomUUID(),
						"공급사"));

		assertTrue(result.summary().contains("요약"));
		assertFalse(result.keywords().isEmpty());
		assertFalse(result.requiresHumanReview());
	}

	@Test
	void shouldRejectNonJsonResponse() {
		ChatModel chatModel = mock(ChatModel.class);
		when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(
				new Generation(new AssistantMessage("그냥 텍스트")))));

		assertThrows(ApplicationException.class, () -> support.enrich(
				chatModel,
				new AiEnrichmentPort.EnrichmentRequest(
						UUID.randomUUID(),
						"상품",
						"설명",
						UUID.randomUUID(),
						"공급사")));
	}
}
