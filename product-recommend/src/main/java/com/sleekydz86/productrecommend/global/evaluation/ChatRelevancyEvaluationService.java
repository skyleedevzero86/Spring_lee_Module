package com.sleekydz86.productrecommend.global.evaluation;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "app.chat", name = "enabled", havingValue = "true")
@ConditionalOnBean(ChatClient.Builder.class)
@ConditionalOnClass(RelevancyEvaluator.class)
public class ChatRelevancyEvaluationService {

	private final RelevancyEvaluator relevancyEvaluator;

	public ChatRelevancyEvaluationService(ChatClient.Builder chatClientBuilder) {
		this.relevancyEvaluator = new RelevancyEvaluator(chatClientBuilder);
	}

	public EvaluationResponse evaluate(String userQuery, String responseText, String retrievedContext) {
		EvaluationRequest request = new EvaluationRequest(
			userQuery,
			List.of(new Document(retrievedContext == null ? "" : retrievedContext)),
			responseText
		);
		return relevancyEvaluator.evaluate(request);
	}
}
