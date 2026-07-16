package com.sleekydz86.catalogflow.domain.model;

import com.sleekydz86.catalogflow.domain.exception.InvalidPriceException;

import java.math.BigDecimal;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {

	public Money {
		Objects.requireNonNull(amount, "amount");
		Objects.requireNonNull(currency, "currency");
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidPriceException("가격은 0보다 작을 수 없습니다");
		}
		if (currency.isBlank()) {
			throw new InvalidPriceException("통화 코드는 비어 있을 수 없습니다");
		}
		amount = amount.stripTrailingZeros();
		currency = currency.trim().toUpperCase();
	}

	public static Money of(long amount, String currency) {
		return new Money(BigDecimal.valueOf(amount), currency);
	}

	public static Money of(BigDecimal amount, String currency) {
		return new Money(amount, currency);
	}
}
