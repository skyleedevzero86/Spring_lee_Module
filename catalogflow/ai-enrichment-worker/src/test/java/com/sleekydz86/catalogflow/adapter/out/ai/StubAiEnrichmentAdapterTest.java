package com.sleekydz86.catalogflow.adapter.out.ai;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import com.sleekydz86.catalogflow.application.port.out.AiEnrichmentPort;
import com.sleekydz86.catalogflow.global.config.AiProperties;
import com.sleekydz86.catalogflow.global.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StubAiEnrichmentAdapterTest {

	private StubAiEnrichmentAdapter adapter;
	private EnrichmentResultValidator validator;

	@BeforeEach
	void setUp() {
		AiProperties properties = new AiProperties();
		adapter = new StubAiEnrichmentAdapter(new StubEnrichmentEngine(properties, new EnrichmentPromptTemplate()));
		validator = new EnrichmentResultValidator();
	}

	@Test
	void shouldGenerateValidStubResult() {
		AiEnrichmentPort.EnrichmentResult result = adapter.enrich(new AiEnrichmentPort.EnrichmentRequest(
				UUID.randomUUID(),
				"무선 키보드",
				"저소음 기계식 키보드",
				UUID.randomUUID(),
				"테스트공급사"));

		validator.validate(result);
		assertTrue(result.summary().contains("무선 키보드"));
		assertFalse(result.keywords().isEmpty());
		assertFalse(result.tags().isEmpty());
	}

	@Test
	void shouldRejectInvalidResult() {
		assertThrows(ApplicationException.class, () -> validator.validate(new AiEnrichmentPort.EnrichmentResult(
				"",
				"",
				"",
				List.of(),
				List.of(),
				"",
				"",
				false,
				1.5d,
				"")));
	}
}
