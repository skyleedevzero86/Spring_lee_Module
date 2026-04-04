package com.sleekydz86.monitoring.backend.monitoring.adapter.out.info;

import com.sleekydz86.monitoring.backend.monitoring.domain.port.CourseCatalogPort;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.ConnectedStoreObservationService;
import com.sleekydz86.monitoring.backend.monitoring.infrastructure.observation.HostSystemObservationService;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MonitoringInfoContributor implements InfoContributor {

    private final CourseCatalogPort courseCatalog;
    private final HostSystemObservationService hostSystemObservationService;
    private final ConnectedStoreObservationService connectedStoreObservationService;
    private final Environment environment;

    public MonitoringInfoContributor(
            CourseCatalogPort courseCatalog,
            HostSystemObservationService hostSystemObservationService,
            ConnectedStoreObservationService connectedStoreObservationService,
            Environment environment
    ) {
        this.courseCatalog = courseCatalog;
        this.hostSystemObservationService = hostSystemObservationService;
        this.connectedStoreObservationService = connectedStoreObservationService;
        this.environment = environment;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> roadmap = new LinkedHashMap<>();
        roadmap.put("sectionCount", this.courseCatalog.sections().size());
        roadmap.put("averageCompletion", this.courseCatalog.averageCompletion());
        roadmap.put("totalMinutes", this.courseCatalog.totalMinutes());
        roadmap.put("sections", this.courseCatalog.sections());

        Map<String, Object> stack = new LinkedHashMap<>();
        stack.put("backend", "Spring Boot 4.0.3 / Java 25");
        stack.put("admin", "Spring Boot Admin 4.0.2 (embedded)");
        stack.put("frontend", "Next.js 15 + TypeScript");
        stack.put("database", "PostgreSQL @ 5433");
        stack.put("redis", "Redis @ 9379");

        HostSystemObservationService.HostSnapshot host = this.hostSystemObservationService.snapshot();
        Map<String, Object> serverEnvironment = new LinkedHashMap<>();
        serverEnvironment.put("hostName", host.hostName());
        serverEnvironment.put("operatingSystemFamily", host.operatingSystemFamily());
        serverEnvironment.put("operatingSystem", host.operatingSystem());
        serverEnvironment.put("architecture", host.architecture());
        serverEnvironment.put("availableProcessors", host.availableProcessors());
        serverEnvironment.put("diskPath", host.diskPath());

        Map<String, Object> connectedStores = new LinkedHashMap<>();
        this.connectedStoreObservationService.snapshots().forEach((store) -> connectedStores.put(
                store.component(),
                Map.of(
                        "status", store.status(),
                        "version", store.version(),
                        "usedBytes", store.usedBytes(),
                        "dailyGrowthBytes", store.dailyGrowthBytes(),
                        "detail", store.detail()
                )
        ));

        Map<String, Object> observability = new LinkedHashMap<>();
        String actuatorBaseUrl = this.environment.getProperty("idolglow.monitoring.actuator-base-url", "http://localhost:8081/actuator");
        observability.put("actuator", actuatorBaseUrl);
        observability.put("healthGroup", actuatorBaseUrl + "/health/infrastructure");
        observability.put("prometheus", this.environment.getProperty("idolglow.monitoring.prometheus-url", "http://localhost:9091"));
        observability.put("grafana", this.environment.getProperty("idolglow.monitoring.grafana-url", "http://localhost:3001"));
        observability.put("historyGuide", "Actuator shows the current moment, Prometheus stores history, Grafana visualizes hourly, daily, weekly, and monthly trends.");

        builder.withDetail("courseRoadmap", roadmap);
        builder.withDetail("monitoringStack", stack);
        builder.withDetail("serverEnvironment", serverEnvironment);
        builder.withDetail("connectedStores", connectedStores);
        builder.withDetail("observability", observability);
    }
}
