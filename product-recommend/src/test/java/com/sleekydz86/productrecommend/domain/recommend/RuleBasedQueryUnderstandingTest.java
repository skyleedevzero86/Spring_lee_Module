package com.sleekydz86.productrecommend.domain.recommend;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RuleBasedQueryUnderstandingTest {

	@Test
	void parsesPriceBrandAndCategoryConstraints() {
		SearchCondition condition = RuleBasedQueryUnderstanding.parse("10만원 이하 검정 러닝화 나이키 말고", 10);
		assertThat(condition.category()).isEqualTo("RUNNING_SHOES");
		assertThat(condition.color()).isEqualTo("BLACK");
		assertThat(condition.excludeBrand()).isEqualTo("NIKE");
		assertThat(condition.maxPrice()).isEqualByComparingTo(BigDecimal.valueOf(100_000));
	}
}
