package com.sleekydz86.searchai.router.application.policy;

import com.sleekydz86.searchai.router.domain.ModelTier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@Order(30)
public final class TranslateRulePolicy implements RoutingRulePolicy {

	@Override
	public String name() {
		return "translate";
	}

	@Override
	public Optional<ModelTier> match(String prompt) {
		String lower = prompt.trim().toLowerCase(Locale.ROOT);
		if (lower.contains("영어로 번역") || lower.contains("translate to english")
			|| lower.contains("한국어로 번역") || lower.startsWith("번역:")) {
			return Optional.of(ModelTier.LUNA);
		}
		return Optional.empty();
	}
}
