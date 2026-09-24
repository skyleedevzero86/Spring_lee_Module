package com.sleekydz86.searchai.router.application.policy;

import com.sleekydz86.searchai.router.domain.ModelTier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Component
@Order(10)
public final class GreetingRulePolicy implements RoutingRulePolicy {

	private static final Set<String> KEYWORDS = Set.of(
		"hi", "hello", "hey", "안녕", "안녕하세요", "반가", "thanks", "thank you", "고마워", "감사합니다"
	);

	@Override
	public String name() {
		return "greeting";
	}

	@Override
	public Optional<ModelTier> match(String prompt) {
		String text = prompt.trim().toLowerCase(Locale.ROOT);
		if (text.length() <= 12) {
			return Optional.of(ModelTier.LUNA);
		}
		for (String keyword : KEYWORDS) {
			if (text.equals(keyword) || text.startsWith(keyword + " ") || text.endsWith(" " + keyword)) {
				return Optional.of(ModelTier.LUNA);
			}
		}
		return Optional.empty();
	}
}
