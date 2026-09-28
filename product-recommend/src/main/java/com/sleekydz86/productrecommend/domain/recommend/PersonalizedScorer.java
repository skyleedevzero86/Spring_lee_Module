package com.sleekydz86.productrecommend.domain.recommend;

import com.sleekydz86.productrecommend.domain.product.Product;
import com.sleekydz86.productrecommend.domain.user.UserPreference;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class PersonalizedScorer {

	private static final double W_VECTOR = 0.35;
	private static final double W_PREF = 0.25;
	private static final double W_KEYWORD = 0.15;
	private static final double W_POP = 0.10;
	private static final double W_FRESH = 0.10;
	private static final double W_BIZ = 0.05;

	private PersonalizedScorer() {
	}

	public static RankedRecommendation score(
		Product product,
		double vectorSimilarity,
		double keywordScore,
		SearchCondition condition,
		UserPreference preference,
		Instant now
	) {
		double pref = preferenceScore(product, preference);
		double pop = normalize(product.popularityScore(), 100);
		double fresh = freshnessScore(product.createdAt(), now);
		double biz = businessScore(product);
		double total = (vectorSimilarity * W_VECTOR)
			+ (pref * W_PREF)
			+ (keywordScore * W_KEYWORD)
			+ (pop * W_POP)
			+ (fresh * W_FRESH)
			+ (biz * W_BIZ);

		List<String> reasons = new ArrayList<>();
		if (vectorSimilarity >= 0.55) {
			reasons.add("검색어/의미 유사도가 높음");
		}
		if (pref >= 0.5 && preference != null) {
			reasons.add("사용자 선호 카테고리/브랜드와 일치");
		}
		if (condition != null && matchesPrice(product, condition)) {
			reasons.add("요청한 가격 조건을 충족");
		}
		if (condition != null && condition.excludeBrand() != null
			&& !Objects.equals(normalize(product.brand()), normalize(condition.excludeBrand()))) {
			reasons.add("제외 브랜드 조건 충족");
		}
		if (keywordScore >= 0.4) {
			reasons.add("상품명/키워드 텍스트 매칭");
		}
		if (pop >= 0.5) {
			reasons.add("인기 상품");
		}
		if (fresh >= 0.6) {
			reasons.add("최근 등록 상품");
		}
		if (reasons.isEmpty()) {
			reasons.add("종합 랭킹 점수 상위 후보");
		}
		return new RankedRecommendation(product, clamp(total), reasons);
	}

	private static double preferenceScore(Product product, UserPreference preference) {
		if (preference == null) {
			return 0.3;
		}
		double score = 0;
		if (preference.preferredCategory() != null
			&& Objects.equals(normalize(preference.preferredCategory()), normalize(product.category()))) {
			score += 0.5;
		}
		if (preference.preferredBrand() != null
			&& Objects.equals(normalize(preference.preferredBrand()), normalize(product.brand()))) {
			score += 0.3;
		}
		if (preference.minPrice() != null && preference.maxPrice() != null
			&& product.price() != null
			&& product.price().compareTo(preference.minPrice()) >= 0
			&& product.price().compareTo(preference.maxPrice()) <= 0) {
			score += 0.2;
		}
		return clamp(score);
	}

	private static boolean matchesPrice(Product product, SearchCondition condition) {
		BigDecimal price = product.price();
		if (price == null) {
			return false;
		}
		if (condition.minPrice() != null && price.compareTo(condition.minPrice()) < 0) {
			return false;
		}
		if (condition.maxPrice() != null && price.compareTo(condition.maxPrice()) > 0) {
			return false;
		}
		return condition.minPrice() != null || condition.maxPrice() != null;
	}

	private static double freshnessScore(Instant createdAt, Instant now) {
		if (createdAt == null) {
			return 0.2;
		}
		long days = Math.max(0, Duration.between(createdAt, now).toDays());
		if (days <= 7) {
			return 1.0;
		}
		if (days <= 30) {
			return 0.7;
		}
		if (days <= 90) {
			return 0.4;
		}
		return 0.2;
	}

	private static double businessScore(Product product) {
		if (!product.available()) {
			return 0;
		}
		if (product.stock() >= 20) {
			return 1.0;
		}
		if (product.stock() >= 5) {
			return 0.7;
		}
		return 0.4;
	}

	private static double normalize(double value, double max) {
		if (max <= 0) {
			return 0;
		}
		return clamp(value / max);
	}

	private static double clamp(double value) {
		return Math.max(0, Math.min(1, value));
	}

	private static String normalize(String value) {
		return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
	}
}
