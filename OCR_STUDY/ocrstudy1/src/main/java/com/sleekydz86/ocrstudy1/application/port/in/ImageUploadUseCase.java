package com.sleekydz86.ocrstudy1.application.port.in;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadUseCase {
    UploadResult uploadAndProcess(MultipartFile file);

    record UploadResult(
            Long imageId,
            String filename,
            String ocrText,
            Boolean hasFace,
            Boolean isIdCard,
            String extractedInfo
    ) {}
}
