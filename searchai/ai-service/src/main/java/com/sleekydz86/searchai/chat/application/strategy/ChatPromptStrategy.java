package com.sleekydz86.searchai.chat.application.strategy;

import com.sleekydz86.searchai.chat.domain.ChatMode;
import reactor.core.publisher.Mono;

public interface ChatPromptStrategy {

	ChatMode supports();

	Mono<String> buildPrompt(String question);
}
