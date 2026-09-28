package com.sleekydz86.productrecommend.domain.recommend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RuleBasedQueryUnderstanding {

	private static final Pattern PRICE_UNDER = Pattern.compile("(\\d+)\\s*만\\s*원\\s*이하|(\\d{4,})\\s*원\\s*이하|under\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
	private static final Pattern EXCLUDE_BRAND = Pattern.compile("(?i)(나이키|nike|아디다스|adidas|뉴발란스|new\\s*balance)\\s*(말고|제외|제외하고)|(?i)(?!.*말고)(?!.*제외)");

	private RuleBasedQueryUnderstanding() {
	}

	public static SearchCondition parse(String rawQuery, int limit) {
		String query = rawQuery == null ? "" : rawQuery.trim();
		String lower = query.toLowerCase(Locale.ROOT);

		String category = null;
		if (containsAny(lower, "러닝", "운동화", "running", "신발", "스니커")) {
			category = "RUNNING_SHOES";
		} else if (containsAny(lower, "노트북", "맥북", "laptop", "맥북")) {
			category = "LAPTOP";
		} else if (containsAny(lower, "이어폰", "헤드폰", "에어팟", "헤드셋")) {
			category = "AUDIO";
		} else if (containsAny(lower, "커피", "원두", "차")) {
			category = "BEVERAGE";
		}

		String color = null;
		if (containsAny(lower, "검정", "블랙", "black")) {
			color = "BLACK";
		} else if (containsAny(lower, "흰", "화이트", "white")) {
			color = "WHITE";
		}

		String brand = null;
		String excludeBrand = null;
		if (containsAny(lower, "나이키 말고", "nike 말고", "나이키 제외", "nike 제외")) {
			excludeBrand = "NIKE";
		} else if (containsAny(lower, "아디다스 말고", "adidas 말고")) {
			excludeBrand = "ADIDAS";
		} else if (containsAny(lower, "나이키", "nike")) {
			brand = "NIKE";
		} else if (containsAny(lower, "아디다스", "adidas")) {
			brand = "ADIDAS";
		} else if (containsAny(lower, "뉴발란스", "new balance")) {
			brand = "NEW_BALANCE";
		} else if (containsAny(lower, "애플", "apple")) {
			brand = "APPLE";
		}

		BigDecimal maxPrice = null;
		Matcher matcher = PRICE_UNDER.matcher(query);
		if (matcher.find()) {
			if (matcher.group(1) != null) {
				maxPrice = BigDecimal.valueOf(Long.parseLong(matcher.group(1)) * 10_000L);
			} else if (matcher.group(2) != null) {
				maxPrice = BigDecimal.valueOf(Long.parseLong(matcher.group(2)));
			} else if (matcher.group(3) != null) {
				maxPrice = BigDecimal.valueOf(Long.parseLong(matcher.group(3)));
			}
		} else if (containsAny(lower, "가성비", "저렴", "싼")) {
			maxPrice = BigDecimal.valueOf(150_000);
		}

		List<String> attributes = new ArrayList<>();
		if (containsAny(lower, "경량", "가벼운", "light")) {
			attributes.add("lightweight");
		}
		if (containsAny(lower, "쿠션", "cushion")) {
			attributes.add("cushion");
		}

		String semantic = query
			.replaceAll("(?i)나이키\\s*말고|nike\\s*말고|제외하고|제외", " ")
			.replaceAll("\\s+", " ")
			.trim();
		if (semantic.isBlank()) {
			semantic = query;
		}

		return new SearchCondition(semantic, category, brand, excludeBrand, color, null, maxPrice, attributes, limit);
	}

	private static boolean containsAny(String text, String... tokens) {
		for (String token : tokens) {
			if (text.contains(token.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}
}
