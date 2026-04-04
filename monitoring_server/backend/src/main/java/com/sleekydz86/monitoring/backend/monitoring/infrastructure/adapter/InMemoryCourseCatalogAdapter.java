package com.sleekydz86.monitoring.backend.monitoring.infrastructure.adapter;

import com.sleekydz86.monitoring.backend.monitoring.domain.course.CourseSection;
import com.sleekydz86.monitoring.backend.monitoring.domain.port.CourseCatalogPort;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCourseCatalogAdapter implements CourseCatalogPort {

    private static final List<CourseSection> SECTIONS = List.of(
            new CourseSection("sec-actuator", "Actuator fundamentals", 6, 120, 88.0, "health", "emerald"),
            new CourseSection("sec-metrics", "Micrometer & Prometheus", 8, 180, 72.5, "metrics", "amber"),
            new CourseSection("sec-admin", "Spring Boot Admin", 5, 90, 91.0, "ui", "violet"));

    @Override
    public List<CourseSection> sections() {
        return SECTIONS;
    }

    @Override
    public double averageCompletion() {
        return SECTIONS.stream().mapToDouble(CourseSection::completionRate).average().orElse(0.0);
    }

    @Override
    public int totalMinutes() {
        return SECTIONS.stream().mapToInt(CourseSection::durationMinutes).sum();
    }

    @Override
    public CourseSection lowestCompletion() {
        return SECTIONS.stream()
                .min(Comparator.comparingDouble(CourseSection::completionRate))
                .orElseThrow();
    }
}
