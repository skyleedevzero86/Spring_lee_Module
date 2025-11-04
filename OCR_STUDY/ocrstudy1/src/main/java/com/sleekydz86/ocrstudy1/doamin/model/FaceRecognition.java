package com.sleekydz86.ocrstudy1.doamin.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceRecognition {
    private Boolean hasFace;
    private Integer faceCount;
    private List<FaceInfo> faces;
    private Double similarityScore;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FaceInfo {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
        private Double confidence;
        private String encoding;
    }
}

