package com.sleekydz86.searchai.usage.application.service;

import com.sleekydz86.searchai.chat.domain.ChatMode;
import com.sleekydz86.searchai.usage.domain.RequestType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestClassifierServiceTest {

	private final RequestClassifierService classifier = new RequestClassifierService();

	@Test
	void classifiesRagFromPdfPrompt() {
		assertThat(classifier.classify(ChatMode.DIRECT, "내가 올린 PDF에서 Spring Batch 설명해줘"))
			.isEqualTo(RequestType.RAG);
	}

	@Test
	void classifiesDbFromUsagePrompt() {
		assertThat(classifier.classify(ChatMode.DIRECT, "내 지난달 AI 사용량 알려줘"))
			.isEqualTo(RequestType.DB);
	}

	@Test
	void classifiesGeneral() {
		assertThat(classifier.classify(ChatMode.DIRECT, "양자컴퓨터 원리를 설명해줘"))
			.isEqualTo(RequestType.GENERAL);
	}

	@Test
	void modeOverridesToRagAndWeb() {
		assertThat(classifier.classify(ChatMode.KNOWLEDGE_BASE, "anything")).isEqualTo(RequestType.RAG);
		assertThat(classifier.classify(ChatMode.INTERNET_SEARCH, "anything")).isEqualTo(RequestType.WEB);
	}
}
