package com.sleekydz86.ocrstudy1.doamin.model;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageComparison {

    private Long currentImageId;
    private Long previousImageId;
    private Double similarityScore;
    private ComparisonType comparisonType;
    private String details;

    public enum ComparisonType {
        FACE_SIMILARITY,
        OCR_TEXT_SIMILARITY,
        VISUAL_SIMILARITY,
        STRUCTURAL_SIMILARITY
    }
}

