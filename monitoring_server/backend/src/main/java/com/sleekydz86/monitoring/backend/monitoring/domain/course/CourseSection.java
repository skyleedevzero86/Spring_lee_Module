package com.sleekydz86.monitoring.backend.monitoring.domain.course;

public record CourseSection(
        String id,
        String title,
        int chapters,
        int durationMinutes,
        double completionRate,
        String focus,
        String accent
) {
}
