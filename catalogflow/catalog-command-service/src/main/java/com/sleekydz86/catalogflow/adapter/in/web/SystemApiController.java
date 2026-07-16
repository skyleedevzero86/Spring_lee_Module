package com.sleekydz86.catalogflow.adapter.in.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/system")
public class SystemApiController {

	@GetMapping
	public Map<String, String> systemInfo() {
		return Map.of(
				"service", "catalog-command-service",
				"role", "main",
				"docs", "/api/v1/system/docs",
				"apiDocs", "/api/v1/system/api-docs");
	}

	@GetMapping("/docs-url")
	public ResponseEntity<Map<String, String>> docsUrl() {
		String docs = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/v1/system/docs")
				.toUriString();
		String apiDocs = ServletUriComponentsBuilder.fromCurrentContextPath()
				.path("/api/v1/system/api-docs")
				.toUriString();
		return ResponseEntity.ok(Map.of("swaggerUi", docs, "openApi", apiDocs));
	}
}
