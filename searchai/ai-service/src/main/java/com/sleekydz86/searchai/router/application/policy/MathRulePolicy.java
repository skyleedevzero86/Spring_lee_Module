package com.sleekydz86.searchai.router.application.policy;

import com.sleekydz86.searchai.router.domain.ModelTier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

@Component
@Order(20)
public final class MathRulePolicy implements RoutingRulePolicy {

	private static final Pattern MATH = Pattern.compile("^[0-9\\s+\\-*/().=]+$");
	private static final Pattern MATH_KR = Pattern.compile(".*(계산|더하기|빼기|곱하기|나누기).*");

	@Override
	public String name() {
		return "math";
	}

	@Override
	public Optional<ModelTier> match(String prompt) {
		String text = prompt.trim();
		String lower = text.toLowerCase(Locale.ROOT);
		if (MATH.matcher(text).matches() || lower.contains("1+1") || MATH_KR.matcher(lower).matches()) {
			return Optional.of(ModelTier.LUNA);
		}
		return Optional.empty();
	}
}
