package com.sleekydz86.searchai.usage.application.service;

import com.sleekydz86.searchai.chat.domain.ChatMode;
import com.sleekydz86.searchai.usage.domain.RequestType;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public final class RequestClassifierService {

	public RequestType classify(ChatMode mode, String message) {
		if (mode == ChatMode.KNOWLEDGE_BASE) {
			return RequestType.RAG;
		}
		if (mode == ChatMode.INTERNET_SEARCH) {
			return RequestType.WEB;
		}
		String text = message == null ? "" : message.trim().toLowerCase(Locale.ROOT);
		if (matches(text, "faq", "자주 묻는", "도움말", "how to reset password")) {
			return RequestType.FAQ;
		}
		if (matches(text, "사용량", "지난달", "내 계정", "budget", "usage", "토큰 사용")) {
			return RequestType.DB;
		}
		if (matches(text, "pdf", "업로드", "문서에서", "지식베이스", "knowledge")) {
			return RequestType.RAG;
		}
		if (matches(text, "요약", "summar")) {
			return RequestType.SUMMARY;
		}
		if (matches(text, "코드", "code", "refactor", "함수", "bug")) {
			return RequestType.CODE;
		}
		if (matches(text, "검색해", "인터넷", "최신 뉴스", "web search")) {
			return RequestType.WEB;
		}
		return RequestType.GENERAL;
	}

	private static boolean matches(String text, String... keywords) {
		for (String keyword : keywords) {
			if (text.contains(keyword.toLowerCase(Locale.ROOT))) {
				return true;
			}
		}
		return false;
	}
}
