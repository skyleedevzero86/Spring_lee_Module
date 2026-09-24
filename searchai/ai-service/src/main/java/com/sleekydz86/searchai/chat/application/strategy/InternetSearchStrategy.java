package com.sleekydz86.searchai.chat.application.strategy;

import com.sleekydz86.searchai.chat.domain.ChatMode;
import com.sleekydz86.searchai.search.application.port.out.InternetSearchPort;
import com.sleekydz86.searchai.search.domain.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Component
public final class InternetSearchStrategy implements ChatPromptStrategy {

	private static final Logger log = LoggerFactory.getLogger(InternetSearchStrategy.class);

	private static final String TEMPLATE = """
		당신은 실시간 인터넷 검색 능력을 갖춘 스마트 비서입니다. 아래 제공된 최신 인터넷 검색 결과를 바탕으로 사용자의 질문에 답변해 주세요.
		규칙:
		1. 모든 검색 결과를 종합적으로 분석하여 사용자에게 포괄적이고 정확하며 일관된 답변을 제공하세요.
		2. 답변에서 "검색 결과에 따르면..."과 같은 문구를 직접 인용하지 말고, 자연스럽게 문장을 구성하세요.
		3. 검색 결과에 충분한 정보가 없다면, "현재 검색 결과로는 질문에 대한 정확한 정보를 찾을 수 없습니다."라고 솔직하게 알려주세요.
		4. 답변은 간결하고 요점을 명확히 해야 합니다.
		【인터넷 검색 결과】
		{context}

		【사용자 질문】
		{question}
		""";

	private final InternetSearchPort internetSearchPort;

	public InternetSearchStrategy(InternetSearchPort internetSearchPort) {
		this.internetSearchPort = internetSearchPort;
	}

	@Override
	public ChatMode supports() {
		return ChatMode.INTERNET_SEARCH;
	}

	@Override
	public Mono<String> buildPrompt(String question) {
		return internetSearchPort.search(question)
			.map(hits -> {
				log.info("인터넷 검색 결과 수: {}", hits.size());
				String context = hits.isEmpty()
					? "유효한 인터넷 검색 결과를 가져오지 못했습니다."
					: formatContext(hits);
				return TEMPLATE.replace("{context}", context).replace("{question}", question);
			});
	}

	private static String formatContext(List<SearchHit> hits) {
		return hits.stream()
			.map(SearchHit::asContextBlock)
			.collect(Collectors.joining("\n\n---\n\n"));
	}
}
