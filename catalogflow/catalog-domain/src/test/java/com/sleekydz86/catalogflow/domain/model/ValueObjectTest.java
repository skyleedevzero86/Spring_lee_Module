package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.exception.InvalidImageReferenceException;
import com.sleekydz86.catalogflow.domain.exception.InvalidProductKeywordException;
import com.sleekydz86.catalogflow.domain.exception.InvalidProductTagException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValueObjectTest {

	@Test
	void shouldNormalizeCurrency() {
		Money money = Money.of(1000, "krw");
		assertEquals("KRW", money.currency());
	}

	@Test
	void shouldRejectInvalidImageReference() {
		assertThrows(InvalidImageReferenceException.class,
				() -> ImageReference.create("", "image/png", 100L, false, Instant.now()));
	}

	@Test
	void shouldNormalizeKeyword() {
		ProductKeyword keyword = new ProductKeyword("  Wireless  ");
		assertEquals("wireless", keyword.value());
	}

	@Test
	void shouldRejectBlankKeyword() {
		assertThrows(InvalidProductKeywordException.class, () -> new ProductKeyword(" "));
	}

	@Test
	void shouldNormalizeTag() {
		ProductTag tag = new ProductTag("  Electronics  ");
		assertEquals("electronics", tag.value());
	}

	@Test
	void shouldRejectBlankTag() {
		assertThrows(InvalidProductTagException.class, () -> new ProductTag(""));
	}
}
