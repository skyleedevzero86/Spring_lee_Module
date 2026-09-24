package com.sleekydz86.searchai.chat.application.strategy;

import com.sleekydz86.searchai.chat.domain.ChatMode;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public final class ChatPromptStrategyFactory {

	private final Map<ChatMode, ChatPromptStrategy> strategies;

	public ChatPromptStrategyFactory(List<ChatPromptStrategy> strategyList) {
		this.strategies = strategyList.stream()
			.collect(Collectors.toMap(
				ChatPromptStrategy::supports,
				Function.identity(),
				(left, right) -> left,
				() -> new EnumMap<>(ChatMode.class)
			));
	}

	public ChatPromptStrategy resolve(ChatMode mode) {
		return Optional.ofNullable(strategies.get(mode))
			.orElseGet(() -> strategies.get(ChatMode.DIRECT));
	}
}
