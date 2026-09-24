package com.sleekydz86.opspilot.incident.adapter.in.web;

import com.sleekydz86.opspilot.global.exception.ResourceNotFoundException;
import com.sleekydz86.opspilot.incident.application.port.in.QueryIncidentUseCase;
import com.sleekydz86.opspilot.incident.application.port.in.TriageIncidentCommand;
import com.sleekydz86.opspilot.incident.application.port.in.TriageIncidentUseCase;
import com.sleekydz86.opspilot.incident.domain.IncidentId;
import com.sleekydz86.opspilot.incident.domain.IncidentReport;
import com.sleekydz86.opspilot.incident.domain.IncidentType;
import com.sleekydz86.opspilot.incident.domain.Severity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public final class IncidentController {

	private final TriageIncidentUseCase triageIncidentUseCase;
	private final QueryIncidentUseCase queryIncidentUseCase;

	public IncidentController(
		TriageIncidentUseCase triageIncidentUseCase,
		QueryIncidentUseCase queryIncidentUseCase
	) {
		this.triageIncidentUseCase = triageIncidentUseCase;
		this.queryIncidentUseCase = queryIncidentUseCase;
	}

	@PostMapping("/triage")
	@ResponseStatus(HttpStatus.CREATED)
	public IncidentResponse triage(@Valid @RequestBody TriageRequest request) {
		IncidentReport report = triageIncidentUseCase.triage(
			new TriageIncidentCommand(
				request.title(),
				request.description(),
				request.type() == null ? IncidentType.UNKNOWN : request.type(),
				request.severity() == null ? Severity.MEDIUM : request.severity(),
				request.source() == null || request.source().isBlank() ? "api" : request.source()
			)
		);
		return IncidentResponse.from(report);
	}

	@GetMapping("/{id}")
	public IncidentResponse findById(@PathVariable UUID id) {
		return queryIncidentUseCase.findById(IncidentId.of(id))
			.map(IncidentResponse::from)
			.orElseThrow(() -> new ResourceNotFoundException("장애를 찾을 수 없습니다: " + id));
	}

	@GetMapping
	public List<IncidentResponse> findRecent(@RequestParam(defaultValue = "20") int limit) {
		return queryIncidentUseCase.findRecent(limit).stream()
			.map(IncidentResponse::from)
			.toList();
	}

	public record TriageRequest(
		@NotBlank(message = "제목이 필요합니다") String title,
		@NotBlank(message = "설명이 필요합니다") String description,
		IncidentType type,
		Severity severity,
		String source
	) {
	}

	public record IncidentResponse(
		String id,
		String title,
		String description,
		String type,
		String severity,
		String status,
		String source,
		String fingerprint,
		boolean urgent,
		double urgencyProbability,
		String category,
		String analysisSeverity,
		String team,
		String summary,
		String recommendedAction,
		String analysisSource,
		boolean cacheHit,
		String model,
		String createdAt,
		String resolvedAt,
		String analyzedAt
	) {

		static IncidentResponse from(IncidentReport report) {
			return new IncidentResponse(
				report.incident().id().toString(),
				report.incident().title(),
				report.incident().description(),
				report.incident().type().name(),
				report.incident().severity().name(),
				report.incident().status().name(),
				report.incident().source(),
				report.incident().fingerprint(),
				report.analysis().urgent(),
				report.analysis().urgencyProbability(),
				report.analysis().category().name(),
				report.analysis().severity().name(),
				report.analysis().team(),
				report.analysis().summary(),
				report.analysis().recommendedAction(),
				report.analysis().analysisSource().name(),
				report.analysis().cacheHit(),
				report.analysis().model(),
				report.incident().createdAt().toString(),
				report.incident().resolvedAt() == null ? null : report.incident().resolvedAt().toString(),
				report.analysis().analyzedAt().toString()
			);
		}
	}
}
