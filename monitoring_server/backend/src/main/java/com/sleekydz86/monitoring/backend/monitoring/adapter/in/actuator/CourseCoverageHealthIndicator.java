package com.sleekydz86.monitoring.backend.monitoring.adapter.in.actuator;

import com.sleekydz86.monitoring.backend.monitoring.domain.course.CourseSection;
import com.sleekydz86.monitoring.backend.monitoring.domain.port.CourseCatalogPort;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("courseCoverage")
public class CourseCoverageHealthIndicator implements HealthIndicator {

    private final CourseCatalogPort courseCatalog;

    public CourseCoverageHealthIndicator(CourseCatalogPort courseCatalog) {
        this.courseCatalog = courseCatalog;
    }

    @Override
    public Health health() {
        double averageCompletion = this.courseCatalog.averageCompletion();
        CourseSection lowestSection = this.courseCatalog.lowestCompletion();
        Health.Builder builder = averageCompletion >= 80.0 ? Health.up() : Health.outOfService();

        return builder
                .withDetail("averageCompletion", averageCompletion)
                .withDetail("sectionCount", this.courseCatalog.sections().size())
                .withDetail("totalMinutes", this.courseCatalog.totalMinutes())
                .withDetail("lowestSection", lowestSection.title())
                .build();
    }
}
