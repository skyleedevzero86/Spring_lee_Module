package com.sleekydz86.searchai.chat.application.strategy;

import com.sleekydz86.searchai.chat.domain.ChatMode;
import com.sleekydz86.searchai.knowledge.application.port.in.SearchKnowledgeUseCase;
import com.sleekydz86.searchai.knowledge.domain.KnowledgeChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public final class KnowledgeBaseStrategy implements ChatPromptStrategy {

	private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseStrategy.class);

	private static final String TEMPLATE = """
		아래 제공된 컨텍스트 지식 베이스 내용을 기반으로 사용자의 질문에 답변해 주세요.
		규칙:
		1. 답변 시 컨텍스트 정보를 충분히 활용하되, "컨텍스트에 따르면", "지식 베이스에 따르면"과 같은 문구를 직접 언급하지 마세요.
		2. 컨텍스트에 질문에 답할 충분한 정보가 없다면, "기존 지식으로는 이 질문에 답변할 수 없습니다."라고 명확하게 알려주세요.
		3. 답변은 직접적이고, 명확하며, 관련성이 있어야 합니다.
		【컨텍스트】
		{context}

		【질문】
		{question}
		""";

	private final SearchKnowledgeUseCase searchKnowledgeUseCase;

	public KnowledgeBaseStrategy(SearchKnowledgeUseCase searchKnowledgeUseCase) {
		this.searchKnowledgeUseCase = searchKnowledgeUseCase;
	}

	@Override
	public ChatMode supports() {
		return ChatMode.KNOWLEDGE_BASE;
	}

	@Override
	public Mono<String> buildPrompt(String question) {
		return searchKnowledgeUseCase.search(question)
			.map(chunks -> {
				log.info("검색된 문서 수: {}", chunks.size());
				String context = chunks.isEmpty()
					? "관련된 지식 베이스 정보를 찾지 못했습니다."
					: formatContext(chunks);
				return TEMPLATE.replace("{context}", context).replace("{question}", question);
			});
	}

	private static String formatContext(List<KnowledgeChunk> chunks) {
		return chunks.stream()
			.map(chunk -> "[" + chunk.fileName() + "]\n" + chunk.text())
			.collect(Collectors.joining("\n---\n"));
	}
}
