package com.sleekydz86.opspilot.incident.application.service;

import com.sleekydz86.opspilot.incident.application.port.in.QueryIncidentUseCase;
import com.sleekydz86.opspilot.incident.application.port.out.IncidentAnalysisRepositoryPort;
import com.sleekydz86.opspilot.incident.application.port.out.IncidentRepositoryPort;
import com.sleekydz86.opspilot.incident.domain.Incident;
import com.sleekydz86.opspilot.incident.domain.IncidentAnalysis;
import com.sleekydz86.opspilot.incident.domain.IncidentId;
import com.sleekydz86.opspilot.incident.domain.IncidentReport;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class QueryIncidentService implements QueryIncidentUseCase {

	private final IncidentRepositoryPort incidentRepository;
	private final IncidentAnalysisRepositoryPort analysisRepository;

	public QueryIncidentService(
		IncidentRepositoryPort incidentRepository,
		IncidentAnalysisRepositoryPort analysisRepository
	) {
		this.incidentRepository = incidentRepository;
		this.analysisRepository = analysisRepository;
	}

	@Override
	public Optional<IncidentReport> findById(IncidentId id) {
		return incidentRepository.findById(id)
			.flatMap(incident -> analysisRepository.findByIncidentId(id)
				.map(analysis -> new IncidentReport(incident, analysis)));
	}

	@Override
	public List<IncidentReport> findRecent(int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 100));
		List<Incident> incidents = incidentRepository.findRecent(safeLimit);
		List<IncidentId> ids = incidents.stream().map(Incident::id).toList();
		Map<IncidentId, IncidentAnalysis> analyses = analysisRepository.findByIncidentIds(ids).stream()
			.collect(Collectors.toMap(IncidentAnalysis::incidentId, Function.identity(), (a, b) -> a));
		return incidents.stream()
			.filter(incident -> analyses.containsKey(incident.id()))
			.map(incident -> new IncidentReport(incident, analyses.get(incident.id())))
			.toList();
	}
}
