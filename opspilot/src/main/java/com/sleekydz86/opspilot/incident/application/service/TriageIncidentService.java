package com.sleekydz86.opspilot.incident.application.service;

import com.sleekydz86.opspilot.incident.application.port.in.TriageIncidentCommand;
import com.sleekydz86.opspilot.incident.application.port.in.TriageIncidentUseCase;
import com.sleekydz86.opspilot.incident.application.port.out.AiUsageRepositoryPort;
import com.sleekydz86.opspilot.incident.application.port.out.FingerprintPort;
import com.sleekydz86.opspilot.incident.application.port.out.IncidentAnalysisRepositoryPort;
import com.sleekydz86.opspilot.incident.application.port.out.IncidentEventPort;
import com.sleekydz86.opspilot.incident.application.port.out.IncidentRepositoryPort;
import com.sleekydz86.opspilot.incident.application.port.out.NotificationPort;
import com.sleekydz86.opspilot.incident.application.port.out.NotificationRepositoryPort;
import com.sleekydz86.opspilot.incident.application.port.out.TriageCachePort;
import com.sleekydz86.opspilot.incident.application.port.out.TriagePort;
import com.sleekydz86.opspilot.incident.domain.AiUsageRecord;
import com.sleekydz86.opspilot.incident.domain.Incident;
import com.sleekydz86.opspilot.incident.domain.IncidentAnalysis;
import com.sleekydz86.opspilot.incident.domain.IncidentReport;
import com.sleekydz86.opspilot.incident.domain.IncidentStatus;
import com.sleekydz86.opspilot.incident.domain.NotificationRecord;
import com.sleekydz86.opspilot.incident.domain.TriageDecision;
import io.micrometer.core.instrument.MeterRegistry;

import java.time.Instant;
import java.util.UUID;

public final class TriageIncidentService implements TriageIncidentUseCase {

	private final FingerprintPort fingerprintPort;
	private final IncidentRepositoryPort incidentRepository;
	private final IncidentAnalysisRepositoryPort analysisRepository;
	private final AiUsageRepositoryPort aiUsageRepository;
	private final NotificationRepositoryPort notificationRepository;
	private final IncidentEventPort incidentEventPort;
	private final TriageCachePort triageCache;
	private final TriagePort triagePort;
	private final NotificationPort notificationPort;
	private final MeterRegistry meterRegistry;

	public TriageIncidentService(
		FingerprintPort fingerprintPort,
		IncidentRepositoryPort incidentRepository,
		IncidentAnalysisRepositoryPort analysisRepository,
		AiUsageRepositoryPort aiUsageRepository,
		NotificationRepositoryPort notificationRepository,
		IncidentEventPort incidentEventPort,
		TriageCachePort triageCache,
		TriagePort triagePort,
		NotificationPort notificationPort,
		MeterRegistry meterRegistry
	) {
		this.fingerprintPort = fingerprintPort;
		this.incidentRepository = incidentRepository;
		this.analysisRepository = analysisRepository;
		this.aiUsageRepository = aiUsageRepository;
		this.notificationRepository = notificationRepository;
		this.incidentEventPort = incidentEventPort;
		this.triageCache = triageCache;
		this.triagePort = triagePort;
		this.notificationPort = notificationPort;
		this.meterRegistry = meterRegistry;
	}

	@Override
	public IncidentReport triage(TriageIncidentCommand command) {
		String fingerprint = fingerprintPort.fingerprint(
			command.source(),
			command.type().name(),
			command.title(),
			command.description()
		);
		Incident incident = incidentRepository.save(
			Incident.open(
				command.title(),
				command.description(),
				command.type(),
				command.severity(),
				command.source(),
				fingerprint
			)
		);
		meterRegistry.counter("incident.created.total").increment();
		incidentEventPort.record(incident.id(), "OPEN", incident.type().name());

		Incident analyzing = incidentRepository.save(incident.withStatus(IncidentStatus.ANALYZING));
		TriageDecision decision;
		try {
			decision = triageCache.find(fingerprint)
				.map(TriageDecision::asCacheHit)
				.orElseGet(() -> evaluateAndCache(analyzing, fingerprint));
			meterRegistry.counter("incident.analysis.success.total").increment();
		}
		catch (RuntimeException ex) {
			meterRegistry.counter("incident.analysis.failure.total").increment();
			incidentRepository.save(analyzing.withStatus(IncidentStatus.FAILED));
			throw ex;
		}

		IncidentAnalysis analysis = analysisRepository.save(
			new IncidentAnalysis(
				UUID.randomUUID(),
				analyzing.id(),
				decision.urgent(),
				decision.urgencyProbability(),
				decision.category(),
				decision.severity(),
				decision.team(),
				decision.summary(),
				decision.recommendedAction(),
				decision.analysisSource(),
				decision.cacheHit(),
				decision.model(),
				decision.inputTokens(),
				decision.outputTokens(),
				Instant.now()
			)
		);

		aiUsageRepository.save(
			new AiUsageRecord(
				UUID.randomUUID(),
				analyzing.id(),
				decision.model(),
				decision.inputTokens(),
				decision.outputTokens(),
				decision.cacheHit(),
				Instant.now()
			)
		);

		Incident updated = incidentRepository.save(
			analyzing.withType(decision.category()).withSeverity(decision.severity())
		);
		incidentEventPort.record(updated.id(), "ANALYZED", analysis.summary());
		NotificationRecord notification = notificationPort.notify(updated, analysis);
		notificationRepository.save(notification);
		Incident resolved = incidentRepository.save(updated.resolve());
		incidentEventPort.record(resolved.id(), "RESOLVED", notification.channel());
		return new IncidentReport(resolved, analysis);
	}

	private TriageDecision evaluateAndCache(Incident incident, String fingerprint) {
		TriageDecision decision = triagePort.evaluate(incident);
		triageCache.put(fingerprint, decision);
		return decision;
	}
}
