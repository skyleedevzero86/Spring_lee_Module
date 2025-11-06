package com.sleekydz86.ocrstudy1.domain.model;

import com.sleekydz86.ocrstudy1.doamin.model.Image;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Image 도메인 모델 테스트")
class ImageTest {

    @Test
    @DisplayName("OCR 텍스트 업데이트")
    void updateOcrText_Success() {
        Image image = Image.builder()
                .originalFilename("test.jpg")
                .storedFilename("test.jpg")
                .filePath("test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName("test.jpg")
                .build();

        image.updateOcrText("새로운 OCR 텍스트");

        assertThat(image.getOcrText()).isEqualTo("새로운 OCR 텍스트");
        assertThat(image.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("얼굴 인코딩 업데이트")
    void updateFaceEncoding_Success() {
        Image image = Image.builder()
                .originalFilename("test.jpg")
                .storedFilename("test.jpg")
                .filePath("test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName("test.jpg")
                .build();

        image.updateFaceEncoding("face_encoding_123");

        assertThat(image.getFaceEncoding()).isEqualTo("face_encoding_123");
        assertThat(image.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("신분증으로 표시")
    void markAsIdCard_Success() {
        Image image = Image.builder()
                .originalFilename("test.jpg")
                .storedFilename("test.jpg")
                .filePath("test.jpg")
                .fileSize(1024L)
                .contentType("image/jpeg")
                .minioObjectName("test.jpg")
                .build();

        image.markAsIdCard("이름: 홍길동\n주민번호: 900101-1234567");

        assertThat(image.getIsIdCard()).isTrue();
        assertThat(image.getExtractedIdInfo()).isEqualTo("이름: 홍길동\n주민번호: 900101-1234567");
        assertThat(image.getUpdatedAt()).isNotNull();
    }
}

