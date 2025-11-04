package com.sleekydz86.ocrstudy1.application.port.in;

import com.sleekydz86.ocrstudy1.doamin.model.ImageComparison;

public interface ImageCompareUseCase {
    ImageComparison compareWithLatest(Long currentImageId);
    ImageComparison compareImages(Long imageId1, Long imageId2);
}