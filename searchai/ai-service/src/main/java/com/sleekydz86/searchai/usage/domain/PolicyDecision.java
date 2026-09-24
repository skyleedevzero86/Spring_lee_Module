package com.sleekydz86.searchai.usage.domain;

import com.sleekydz86.searchai.router.domain.ModelTier;

public record PolicyDecision(
	boolean allowed,
	boolean generalBlocked,
	boolean llmRequired,
	BudgetState budgetState,
	ModelTier maxAllowedTier,
	long maxOutputTokens,
	String message
) {

	public static PolicyDecision allow(BudgetState state, ModelTier maxTier, long maxOutputTokens) {
		return new PolicyDecision(true, false, true, state, maxTier, maxOutputTokens, "");
	}

	public static PolicyDecision allowInternal(BudgetState state, String message) {
		return new PolicyDecision(true, true, false, state, ModelTier.LUNA, 0, message);
	}

	public static PolicyDecision deny(String message) {
		return new PolicyDecision(false, true, true, BudgetState.HARD, ModelTier.LUNA, 0, message);
	}

	public static PolicyDecision blockExternalLlm(BudgetState state, ModelTier maxTier, long maxOutputTokens, String message) {
		return new PolicyDecision(true, true, true, state, maxTier, maxOutputTokens, message);
	}
}
