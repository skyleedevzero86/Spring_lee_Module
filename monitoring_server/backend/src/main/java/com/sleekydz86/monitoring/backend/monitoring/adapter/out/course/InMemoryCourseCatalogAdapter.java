package com.sleekydz86.monitoring.backend.monitoring.adapter.out.course;

import com.sleekydz86.monitoring.backend.monitoring.domain.course.CourseSection;
import com.sleekydz86.monitoring.backend.monitoring.domain.port.CourseCatalogPort;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;

@Service
public class InMemoryCourseCatalogAdapter implements CourseCatalogPort {

    private final List<CourseSection> sections = List.of(
            new CourseSection("section-2", "엔드포인트 기초와 설정", 3, 44, 82.0, "노출 범위, 의존성 설정, 커스텀 엔드포인트", "#7dd3fc"),
            new CourseSection("section-3", "Health / Info 엔드포인트", 2, 48, 88.0, "상태 점검과 운영 정보 공개", "#a3e635"),
            new CourseSection("section-4", "메트릭 심화 1", 4, 59, 91.0, "카운터, 태그, 공통 태그", "#fbbf24"),
            new CourseSection("section-5", "메트릭 심화 2", 4, 61, 86.0, "게이지, 타이머, @Timed, 백분위", "#fb7185"),
            new CourseSection("section-6", "Spring Boot Admin 연동", 2, 36, 90.0, "중앙 모니터링 UI와 운영 흐름", "#38bdf8")
    );

    @Override
    public List<CourseSection> sections() {
        return this.sections;
    }

    @Override
    public double averageCompletion() {
        return this.sections.stream()
                .mapToDouble(CourseSection::completionRate)
                .average()
                .orElse(0.0);
    }

    @Override
    public int totalMinutes() {
        return this.sections.stream()
                .mapToInt(CourseSection::durationMinutes)
                .sum();
    }

    @Override
    public CourseSection lowestCompletion() {
        return this.sections.stream()
                .min(Comparator.comparingDouble(CourseSection::completionRate))
                .orElse(this.sections.getFirst());
    }
}
