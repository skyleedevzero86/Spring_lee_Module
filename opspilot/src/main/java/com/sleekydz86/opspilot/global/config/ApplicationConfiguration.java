package com.sleekydz86.opspilot.global.config;

import com.sleekydz86.opspilot.incident.adapter.out.cache.OpsPilotCacheProperties;
import com.sleekydz86.opspilot.incident.application.port.in.QueryIncidentUseCase;
import com.sleekydz86.opspilot.incident.application.port.in.QueryStatsUseCase;
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
import com.sleekydz86.opspilot.incident.application.service.QueryIncidentService;
import com.sleekydz86.opspilot.incident.application.service.QueryStatsService;
import com.sleekydz86.opspilot.incident.application.service.TriageIncidentService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(OpsPilotCacheProperties.class)
public class ApplicationConfiguration {

	@Bean
	TriageIncidentUseCase triageIncidentUseCase(
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
		return new TriageIncidentService(
			fingerprintPort,
			incidentRepository,
			analysisRepository,
			aiUsageRepository,
			notificationRepository,
			incidentEventPort,
			triageCache,
			triagePort,
			notificationPort,
			meterRegistry
		);
	}

	@Bean
	QueryIncidentUseCase queryIncidentUseCase(
		IncidentRepositoryPort incidentRepository,
		IncidentAnalysisRepositoryPort analysisRepository
	) {
		return new QueryIncidentService(incidentRepository, analysisRepository);
	}

	@Bean
	QueryStatsUseCase queryStatsUseCase(
		IncidentRepositoryPort incidentRepository,
		IncidentAnalysisRepositoryPort analysisRepository
	) {
		return new QueryStatsService(incidentRepository, analysisRepository);
	}

	@Bean
	WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/api/**")
					.allowedOrigins("http://localhost:5173", "http://127.0.0.1:5173")
					.allowedMethods("GET", "POST", "OPTIONS")
					.allowedHeaders("*");
			}
		};
	}
}
