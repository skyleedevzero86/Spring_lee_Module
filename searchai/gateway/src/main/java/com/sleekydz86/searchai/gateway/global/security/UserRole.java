package com.sleekydz86.searchai.gateway.global.security;

public enum UserRole {
	ADMIN,
	USER;

	public String authority() {
		return "ROLE_" + name();
	}

	public boolean isAdmin() {
		return this == ADMIN;
	}

	public static UserRole fromAuthority(String authority) {
		if (authority == null) {
			return USER;
		}
		String normalized = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
		return UserRole.valueOf(normalized.toUpperCase());
	}
}
