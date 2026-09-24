package com.sleekydz86.searchai.chat.application.strategy;

import com.sleekydz86.searchai.chat.domain.ChatMode;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class DirectChatStrategy implements ChatPromptStrategy {

	@Override
	public ChatMode supports() {
		return ChatMode.DIRECT;
	}

	@Override
	public Mono<String> buildPrompt(String question) {
		return Mono.just(question);
	}
}
