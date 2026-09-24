package com.sleekydz86.searchai.chat.domain;

public enum ChatMode {
	DIRECT,
	KNOWLEDGE_BASE,
	INTERNET_SEARCH;

	public static ChatMode from(String value) {
		if (value == null || value.isBlank()) {
			return DIRECT;
		}
		return ChatMode.valueOf(value.trim().toUpperCase());
	}
}
