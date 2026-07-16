package com.sleekydz86.catalogflow.adapter.out.ai;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.global.config.AiProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;

@Component
public class ChatModelEnrichmentSupport {

	private final EnrichmentPromptTemplate enrichmentPromptTemplate;
	private final EnrichmentResultValidator enrichmentResultValidator;
	private final AiProperties aiProperties;
	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	public ChatModelEnrichmentSupport(
			EnrichmentPromptTemplate enrichmentPromptTemplate,
			EnrichmentResultValidator enrichmentResultValidator,
			AiProperties aiProperties) {
		this.enrichmentPromptTemplate = enrichmentPromptTemplate;
		this.enrichmentResultValidator = enrichmentResultValidator;
		this.aiProperties = aiProperties;
	}

	public AiEnrichmentPort.EnrichmentResult enrich(ChatModel chatModel, AiEnrichmentPort.EnrichmentRequest request) {
		try {
			Prompt prompt = new Prompt(List.of(
					new SystemMessage(enrichmentPromptTemplate.renderSystemPrompt()),
					new UserMessage(enrichmentPromptTemplate.renderUserPrompt(request))));
			ChatResponse response = chatModel.call(prompt);
			String content = response.getResult() == null || response.getResult().getOutput() == null
					? ""
					: response.getResult().getOutput().getText();
			AiEnrichmentPort.EnrichmentResult result = parse(content);
			enrichmentResultValidator.validate(result);
			return result;
		}
		catch (ApplicationException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ApplicationException("LLM 상품 가공에 실패했습니다", exception);
		}
	}

	private AiEnrichmentPort.EnrichmentResult parse(String content) {
		try {
			String json = extractJson(content);
			JsonNode root = objectMapper.readTree(json);
			List<String> keywords = readStringList(root, "keywords");
			List<String> tags = readStringList(root, "tags");
			return new AiEnrichmentPort.EnrichmentResult(
					aiProperties.getModelName(),
					text(root, "summary"),
					text(root, "generatedDescription"),
					keywords,
					tags,
					text(root, "recommendedCategory"),
					text(root, "warnings"),
					root.path("requiresHumanReview").asBoolean(true),
					root.path("confidence").asDouble(0.7d),
					aiProperties.getPromptVersion());
		}
		catch (ApplicationException exception) {
			throw exception;
		}
		catch (Exception exception) {
			throw new ApplicationException("AI 응답 JSON 파싱에 실패했습니다", exception);
		}
	}

	private String extractJson(String content) {
		if (content == null || content.isBlank()) {
			throw new ApplicationException("AI 응답이 비어 있습니다");
		}
		String trimmed = content.trim();
		int start = trimmed.indexOf('{');
		int end = trimmed.lastIndexOf('}');
		if (start < 0 || end <= start) {
			throw new ApplicationException("AI 응답에서 JSON 객체를 찾을 수 없습니다");
		}
		return trimmed.substring(start, end + 1);
	}

	private String text(JsonNode root, String field) {
		JsonNode node = root.get(field);
		if (node == null || node.isNull()) {
			return "";
		}
		return node.asText();
	}

	private List<String> readStringList(JsonNode root, String field) {
		JsonNode node = root.get(field);
		if (node == null || node.isNull() || !node.isArray()) {
			return List.of();
		}
		List<String> values = new ArrayList<>();
		node.forEach(item -> {
			if (item != null && !item.asText().isBlank()) {
				values.add(item.asText().trim());
			}
		});
		return values;
	}
}
