package com.sleekydz86.loginstudy.auth.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class AuthSecurityMetrics {

	private final Counter loginSuccess;
	private final Counter loginFailure;
	private final Counter tokenIssued;
	private final Counter tokenIssueFailure;

	public AuthSecurityMetrics(MeterRegistry meterRegistry) {
		this.loginSuccess = Counter.builder("loginstudy.auth.login")
				.description("폼 로그인 성공/실패 횟수")
				.tag("result", "success")
				.register(meterRegistry);
		this.loginFailure = Counter.builder("loginstudy.auth.login")
				.description("폼 로그인 성공/실패 횟수")
				.tag("result", "failure")
				.register(meterRegistry);
		this.tokenIssued = Counter.builder("loginstudy.auth.token")
				.description("OAuth2 토큰 발급 성공/실패 횟수")
				.tag("result", "success")
				.register(meterRegistry);
		this.tokenIssueFailure = Counter.builder("loginstudy.auth.token")
				.description("OAuth2 토큰 발급 성공/실패 횟수")
				.tag("result", "failure")
				.register(meterRegistry);
	}

	public void incrementLoginSuccess() {
		loginSuccess.increment();
	}

	public void incrementLoginFailure() {
		loginFailure.increment();
	}

	public void incrementTokenIssued() {
		tokenIssued.increment();
	}

	public void incrementTokenIssueFailure() {
		tokenIssueFailure.increment();
	}
}
