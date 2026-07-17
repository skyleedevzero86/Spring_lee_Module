package com.sleekydz86.loginstudy.userportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenMaskingUnitTest {

	@Test
	@DisplayName("토큰 마스킹은 원본 토큰 전체를 반환하지 않는다")
	void maskTokenNeverReturnsFullRawToken() {
		// given
		String rawToken = "aaaaaaaa.bbbbbbbb.cccccccc";

		// when
		String masked = HomeController.maskToken(rawToken);

		// then
		assertThat(masked).doesNotContain(rawToken);
		assertThat(masked).startsWith("aaaaaaaa");
		assertThat(masked).endsWith("cccccc");
		assertThat(masked).contains("...");
	}

	@Test
	@DisplayName("토큰 마스킹은 짧은 민감 값을 숨긴다")
	void maskTokenNeutralizesShortSensitiveValues() {
		// given
		String shortSecret = "<script>";

		// when
		String masked = HomeController.maskToken(shortSecret);

		// then
		assertThat(masked).isEqualTo("***");
	}
}
