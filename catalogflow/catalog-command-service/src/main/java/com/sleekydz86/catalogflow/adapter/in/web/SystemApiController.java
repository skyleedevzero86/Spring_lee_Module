package com.sleekydz86.catalogflow.adapter.in.web;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/system")
public class SystemApiController {

	@Value("${server.port:8081}")
	private int serverPort;

	@GetMapping
	public Map<String, Object> systemInfo() {
		return Map.of(
				"service", "catalog-command-service",
				"role", "main",
				"port", serverPort,
				"docs", "/api/v1/system/docs",
				"apiDocs", "/api/v1/system/api-docs",
				"metrics", "/actuator/prometheus");
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
