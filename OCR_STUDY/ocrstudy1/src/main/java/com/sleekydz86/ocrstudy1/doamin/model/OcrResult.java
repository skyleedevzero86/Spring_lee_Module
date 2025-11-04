package com.sleekydz86.ocrstudy1.doamin.model;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    private String text;
    private List<BoundingBox> boundingBoxes;
    private Double confidence;
    private String language;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BoundingBox {
        private Double x;
        private Double y;
        private Double width;
        private Double height;
        private String text;
        private Double confidence;
    }
}

