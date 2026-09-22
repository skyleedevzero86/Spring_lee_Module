package com.sleekydz86.failurelab.scenario.domain;

public enum ServiceKind {

	PROFILE("Profile"),
	ORDERS("Orders"),
	RECOMMENDATION("Recommendation");

	private final String displayName;

	ServiceKind(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
