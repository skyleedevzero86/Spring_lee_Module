package com.sleekydz86.searchai.chat.domain;

import com.sleekydz86.searchai.usage.domain.RequestType;

public record ChatQuery(
	String userId,
	String username,
	String message,
	ChatMode mode,
	String routingMode,
	String forcedTier
) {

	public ChatQuery {
		if (userId == null || userId.isBlank()) {
			throw new IllegalArgumentException("사용자 ID는 필수입니다");
		}
		if (message == null || message.isBlank()) {
			throw new IllegalArgumentException("메시지는 필수입니다");
		}
		username = username == null || username.isBlank() ? userId : username;
		mode = mode == null ? ChatMode.DIRECT : mode;
		routingMode = routingMode == null || routingMode.isBlank() ? "AUTO" : routingMode.toUpperCase();
	}

	public static ChatQuery of(String userId, String username, String message, ChatMode mode, String routingMode, String forcedTier) {
		return new ChatQuery(userId, username, message, mode, routingMode, forcedTier);
	}

	public RequestType requestType() {
		return switch (mode) {
			case KNOWLEDGE_BASE -> RequestType.RAG;
			case INTERNET_SEARCH -> RequestType.WEB;
			case DIRECT -> inferGeneral(message);
		};
	}

	private static RequestType inferGeneral(String message) {
		String lower = message.toLowerCase();
		if (lower.contains("요약") || lower.contains("summar")) {
			return RequestType.SUMMARY;
		}
		if (lower.contains("코드") || lower.contains("code") || lower.contains("refactor")) {
			return RequestType.CODE;
		}
		return RequestType.GENERAL;
	}

	public boolean isManualRouting() {
		return "MANUAL".equalsIgnoreCase(routingMode);
	}
}
