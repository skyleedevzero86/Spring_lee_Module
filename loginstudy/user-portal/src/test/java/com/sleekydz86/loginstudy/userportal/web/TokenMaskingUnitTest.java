package com.sleekydz86.loginstudy.userportal.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenMaskingUnitTest {

	@Test
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
	void maskTokenNeutralizesShortSensitiveValues() {
		// given
		String shortSecret = "<script>";

		// when
		String masked = HomeController.maskToken(shortSecret);

		// then
		assertThat(masked).isEqualTo("***");
	}
}
