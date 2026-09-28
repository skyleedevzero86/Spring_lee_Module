package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.adapter.in.mcp.ProductSearchTool;
import com.sleekydz86.productrecommend.application.port.in.ProductUseCase;
import com.sleekydz86.productrecommend.domain.product.RecommendationResponse;
import com.sleekydz86.productrecommend.domain.recommend.RankedRecommendation;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
@ConditionalOnBean(ChatClient.class)
public class ProductChatService {

	private static final String SYSTEM_PROMPT = """
		당신은 친절한 상품 추천 어시스턴트입니다.
		도구로 검색한 결과와 추천 근거(reasons)만 사용해 설명하세요.
		근거에 없는 내용을 지어내지 마세요.
		응답은 productIds와 explanation, evidences를 채우세요.
		""";

	private final ChatClient chatClient;
	private final ProductSearchTool productSearchTool;
	private final ProductUseCase productUseCase;
	private final ObjectProvider<ChatMemory> chatMemory;
	private final ObjectProvider<VectorStore> vectorStore;

	public ProductChatService(
		ChatClient chatClient,
		ProductSearchTool productSearchTool,
		ProductUseCase productUseCase,
		ObjectProvider<ChatMemory> chatMemory,
		ObjectProvider<VectorStore> vectorStore
	) {
		this.chatClient = chatClient;
		this.productSearchTool = productSearchTool;
		this.productUseCase = productUseCase;
		this.chatMemory = chatMemory;
		this.vectorStore = vectorStore;
	}

	public StructuredChatResponse chat(String userMessage, String conversationId, String userId) {
		List<RankedRecommendation> ranked = productUseCase.recommend(userId, userMessage, 5);
		List<RecommendationResponse.EvidenceItem> evidences = ranked.stream()
			.map(r -> new RecommendationResponse.EvidenceItem(r.product().id(), r.score(), r.reasons()))
			.toList();
		String evidenceContext = evidences.stream()
			.map(e -> "productId=" + e.productId() + ", reasons=" + String.join(" / ", e.reasons()))
			.reduce((a, b) -> a + "\n" + b)
			.orElse("추천 후보 없음");

		ChatMemory memory = chatMemory.getIfAvailable();
		VectorStore store = vectorStore.getIfAvailable();
		RecommendationResponse recommendation;
		String prompt = userMessage + "\n\n[RecommendationEvidence]\n" + evidenceContext;
		if (memory != null && store != null) {
			recommendation = chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(prompt)
				.tools(productSearchTool)
				.advisors(
					MessageChatMemoryAdvisor.builder(memory).build(),
					VectorStoreChatMemoryAdvisor.builder(store).build()
				)
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.call()
				.entity(RecommendationResponse.class);
		} else if (memory != null) {
			recommendation = chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(prompt)
				.tools(productSearchTool)
				.advisors(MessageChatMemoryAdvisor.builder(memory).build())
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.call()
				.entity(RecommendationResponse.class);
		} else {
			recommendation = chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(prompt)
				.tools(productSearchTool)
				.call()
				.entity(RecommendationResponse.class);
		}

		if (recommendation == null || recommendation.productIds() == null || recommendation.productIds().isEmpty()) {
			recommendation = new RecommendationResponse(
				ranked.stream().map(r -> r.product().id()).toList(),
				recommendation == null ? "추천 결과를 정리했습니다." : recommendation.explanation(),
				evidences
			);
		} else if (recommendation.evidences() == null || recommendation.evidences().isEmpty()) {
			recommendation = new RecommendationResponse(
				recommendation.productIds(),
				recommendation.explanation(),
				evidences
			);
		}

		String message = recommendation.explanation() == null ? "" : recommendation.explanation();
		return new StructuredChatResponse(message, recommendation);
	}

	public Flux<String> chatStream(String userMessage, String conversationId, String userId) {
		ChatMemory memory = chatMemory.getIfAvailable();
		VectorStore store = vectorStore.getIfAvailable();
		String prompt = userMessage;
		List<RankedRecommendation> ranked = productUseCase.recommend(userId, userMessage, 5);
		String evidenceContext = ranked.stream()
			.map(r -> "productId=" + r.product().id() + ", reasons=" + String.join(" / ", r.reasons()))
			.reduce((a, b) -> a + "\n" + b)
			.orElse("추천 후보 없음");
		prompt = userMessage + "\n\n[RecommendationEvidence]\n" + evidenceContext;

		if (memory != null && store != null) {
			return chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(prompt)
				.tools(productSearchTool)
				.advisors(
					MessageChatMemoryAdvisor.builder(memory).build(),
					VectorStoreChatMemoryAdvisor.builder(store).build()
				)
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.stream()
				.content();
		}
		if (memory != null) {
			return chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(prompt)
				.tools(productSearchTool)
				.advisors(MessageChatMemoryAdvisor.builder(memory).build())
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.stream()
				.content();
		}
		return chatClient.prompt()
			.system(SYSTEM_PROMPT)
			.user(prompt)
			.tools(productSearchTool)
			.stream()
			.content();
	}
}
