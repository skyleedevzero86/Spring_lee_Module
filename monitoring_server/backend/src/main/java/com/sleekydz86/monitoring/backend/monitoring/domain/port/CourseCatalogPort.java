package com.sleekydz86.monitoring.backend.monitoring.domain.port;

import com.sleekydz86.monitoring.backend.monitoring.domain.course.CourseSection;
import java.util.List;

public interface CourseCatalogPort {

    List<CourseSection> sections();

    double averageCompletion();

    int totalMinutes();

    CourseSection lowestCompletion();
}
