package com.sleekydz86.searchai.gateway.global.security;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public final class ChatModeAuthorization {

	public void assertAllowed(UserRole role, String mode) {
		String normalized = mode == null || mode.isBlank() ? "DIRECT" : mode.trim().toUpperCase();
		if ("DIRECT".equals(normalized)) {
			return;
		}
		if (!role.isAdmin()) {
			throw new ResponseStatusException(
				HttpStatus.FORBIDDEN,
				"일반 사용자는 채팅만 사용할 수 있습니다. 지식베이스/검색엔진은 관리자 전용입니다."
			);
		}
		if (!"KNOWLEDGE_BASE".equals(normalized) && !"INTERNET_SEARCH".equals(normalized)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 채팅 모드입니다");
		}
	}

	public void assertPdfUpload(UserRole role, String fileName) {
		if (!role.isAdmin()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "PDF 업로드는 관리자만 가능합니다");
		}
		if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "관리자는 PDF 파일만 업로드할 수 있습니다");
		}
	}
}
