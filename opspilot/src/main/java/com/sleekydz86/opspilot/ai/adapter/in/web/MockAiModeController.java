package com.sleekydz86.opspilot.ai.adapter.in.web;

import com.sleekydz86.opspilot.ai.application.MockAiModeState;
import com.sleekydz86.opspilot.ai.domain.MockAiMode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/mock-ai")
public final class MockAiModeController {

	private final MockAiModeState modeState;

	public MockAiModeController(MockAiModeState modeState) {
		this.modeState = modeState;
	}

	@GetMapping("/mode")
	public Map<String, String> current() {
		return Map.of("mode", modeState.current().name());
	}

	@PostMapping("/mode/{mode}")
	@ResponseStatus(HttpStatus.OK)
	public Map<String, String> switchMode(@PathVariable MockAiMode mode) {
		return Map.of("mode", modeState.switchTo(mode).name());
	}
}
