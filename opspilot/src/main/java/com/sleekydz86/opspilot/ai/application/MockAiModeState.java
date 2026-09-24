package com.sleekydz86.opspilot.ai.application;

import com.sleekydz86.opspilot.ai.domain.MockAiMode;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

@Component
public final class MockAiModeState {

	private final AtomicReference<MockAiMode> mode = new AtomicReference<>(MockAiMode.NORMAL);

	public MockAiMode current() {
		return mode.get();
	}

	public MockAiMode switchTo(MockAiMode next) {
		if (next == null) {
			throw new IllegalArgumentException("Mock AI 모드가 필요합니다");
		}
		mode.set(next);
		return next;
	}
}
