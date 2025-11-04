package com.sleekydz86.ocrstudy1.doamin.model;

import lombok.*;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAnalysis {
    private Long imageId;
    private String summary;
    private Double authenticityScore;
    private Boolean isAuthentic;
    private List<String> detectedIssues;
    private Map<String, Object> extractedFields;
    private String aiInsights;
    private Double confidence;
}

