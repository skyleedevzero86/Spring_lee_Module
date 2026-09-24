package com.sleekydz86.searchai.chat.application.service;

import com.sleekydz86.searchai.chat.domain.MockAiMode;
import com.sleekydz86.searchai.global.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicReference;

@Service
public final class MockAiFaultService {

	private final AtomicReference<MockAiMode> mode;

	public MockAiFaultService(AppProperties appProperties) {
		MockAiMode initial = appProperties.mockAi() == null
			? MockAiMode.NORMAL
			: MockAiMode.from(appProperties.mockAi().modeOrDefault());
		this.mode = new AtomicReference<>(initial);
	}

	public MockAiMode current() {
		return mode.get();
	}

	public MockAiMode update(String value) {
		MockAiMode next = MockAiMode.from(value);
		mode.set(next);
		return next;
	}
}
