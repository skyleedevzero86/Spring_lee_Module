package com.sleekydz86.searchai.router.application.policy;

import com.sleekydz86.searchai.router.domain.ModelTier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;

@Component
@Order(40)
public final class FormatRulePolicy implements RoutingRulePolicy {

	@Override
	public String name() {
		return "format";
	}

	@Override
	public Optional<ModelTier> match(String prompt) {
		String lower = prompt.trim().toLowerCase(Locale.ROOT);
		if (lower.contains("json 포맷") || lower.contains("json format") || lower.contains("json으로 정리")
			|| lower.contains("포맷 정리") || lower.contains("pretty print")) {
			return Optional.of(ModelTier.LUNA);
		}
		return Optional.empty();
	}
}
