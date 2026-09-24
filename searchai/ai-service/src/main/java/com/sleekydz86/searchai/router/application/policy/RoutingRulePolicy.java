package com.sleekydz86.searchai.router.application.policy;

import com.sleekydz86.searchai.router.domain.ModelTier;

import java.util.Optional;

public interface RoutingRulePolicy {

	String name();

	Optional<ModelTier> match(String prompt);
}
