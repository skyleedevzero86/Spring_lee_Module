package com.sleekydz86.searchai.chat.adapter.in.web;

import com.sleekydz86.searchai.chat.application.service.MockAiFaultService;
import com.sleekydz86.searchai.chat.domain.MockAiMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/mock-ai")
public final class MockAiFaultController {

	private final MockAiFaultService mockAiFaultService;

	public MockAiFaultController(MockAiFaultService mockAiFaultService) {
		this.mockAiFaultService = mockAiFaultService;
	}

	@GetMapping("/mode")
	public Map<String, String> current() {
		return Map.of("mode", mockAiFaultService.current().name());
	}

	@PostMapping("/mode/{mode}")
	public Map<String, String> update(@PathVariable String mode) {
		MockAiMode updated = mockAiFaultService.update(mode);
		return Map.of("mode", updated.name());
	}
}
