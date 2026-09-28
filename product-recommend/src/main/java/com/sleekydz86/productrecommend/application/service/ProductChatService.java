package com.sleekydz86.productrecommend.application.service;

import com.sleekydz86.productrecommend.adapter.in.mcp.ProductSearchTool;
import com.sleekydz86.productrecommend.domain.product.RecommendationResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.VectorStoreChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@ConditionalOnBean(ChatClient.class)
public class ProductChatService {

	private static final String SYSTEM_PROMPT = """
		당신은 친절한 상품 추천 어시스턴트입니다.
		사용자의 요구사항과 선호도에 맞는 상품을 찾는 것을 도와줍니다.

		사용자가 상품에 대해 질문하면, 사용 가능한 도구를 사용하여 상품을 검색하세요:
		- searchProducts: 키워드로 상품 검색
		- findSimilarProducts: 특정 상품과 유사한 상품 찾기
		- getProductById: 특정 상품의 상세 정보 조회
		- getAllProducts: 모든 상품 목록 조회

		검색 결과를 바탕으로 항상 도움이 되는 추천을 제공하세요.
		구조화 응답에서는 productIds에 추천 상품 ID를, explanation에 이유를 담으세요.
		상품을 찾지 못한 경우, 사용자에게 알리고 대안적인 검색어를 제안하세요.
		이전 대화 내용을 기억하고 맥락에 맞는 응답을 제공하세요.
		""";

	private final ChatClient chatClient;
	private final ProductSearchTool productSearchTool;
	private final ObjectProvider<ChatMemory> chatMemory;
	private final ObjectProvider<VectorStore> vectorStore;

	public ProductChatService(
		ChatClient chatClient,
		ProductSearchTool productSearchTool,
		ObjectProvider<ChatMemory> chatMemory,
		ObjectProvider<VectorStore> vectorStore
	) {
		this.chatClient = chatClient;
		this.productSearchTool = productSearchTool;
		this.chatMemory = chatMemory;
		this.vectorStore = vectorStore;
	}

	public StructuredChatResponse chat(String userMessage, String conversationId) {
		ChatMemory memory = chatMemory.getIfAvailable();
		VectorStore store = vectorStore.getIfAvailable();
		RecommendationResponse recommendation;
		if (memory != null && store != null) {
			recommendation = chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(userMessage)
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
				.user(userMessage)
				.tools(productSearchTool)
				.advisors(MessageChatMemoryAdvisor.builder(memory).build())
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.call()
				.entity(RecommendationResponse.class);
		} else {
			recommendation = chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(userMessage)
				.tools(productSearchTool)
				.call()
				.entity(RecommendationResponse.class);
		}
		String message = recommendation == null || recommendation.explanation() == null
			? ""
			: recommendation.explanation();
		return new StructuredChatResponse(message, recommendation);
	}

	public Flux<String> chatStream(String userMessage, String conversationId) {
		ChatMemory memory = chatMemory.getIfAvailable();
		VectorStore store = vectorStore.getIfAvailable();
		if (memory != null && store != null) {
			return chatClient.prompt()
				.system(SYSTEM_PROMPT)
				.user(userMessage)
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
				.user(userMessage)
				.tools(productSearchTool)
				.advisors(MessageChatMemoryAdvisor.builder(memory).build())
				.advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
				.stream()
				.content();
		}
		return chatClient.prompt()
			.system(SYSTEM_PROMPT)
			.user(userMessage)
			.tools(productSearchTool)
			.stream()
			.content();
	}
}
