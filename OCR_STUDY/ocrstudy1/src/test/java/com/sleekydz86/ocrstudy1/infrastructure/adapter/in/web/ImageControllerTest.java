package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.web;

import com.sleekydz86.ocrstudy1.application.port.in.ImageCompareUseCase;
import com.sleekydz86.ocrstudy1.application.port.in.ImageUploadUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.ImageComparison;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageController 테스트")
class ImageControllerTest {

    @Mock
    private ImageUploadUseCase imageUploadUseCase;

    @Mock
    private ImageCompareUseCase imageCompareUseCase;

    @InjectMocks
    private ImageController imageController;

    private MockMultipartFile mockFile;
    private ImageUploadUseCase.UploadResult uploadResult;

    @BeforeEach
    void setUp() {
        mockFile = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        uploadResult = new ImageUploadUseCase.UploadResult(
                1L,
                "test.jpg",
                "OCR 텍스트",
                false,
                false,
                null
        );
    }

    @Test
    @DisplayName("이미지 업로드 성공")
    void uploadImage_Success() {
        when(imageUploadUseCase.uploadAndProcess(any(MultipartFile.class)))
                .thenReturn(uploadResult);

        ResponseEntity<Map<String, Object>> response = imageController.uploadImage(mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);
        assertThat(response.getBody().get("message")).isEqualTo("파일 업로드 및 처리 완료");

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("imageId")).isEqualTo(1L);
        assertThat(data.get("filename")).isEqualTo("test.jpg");

        verify(imageUploadUseCase).uploadAndProcess(any(MultipartFile.class));
    }

    @Test
    @DisplayName("이미지 업로드 실패 - 빈 파일")
    void uploadImage_EmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[0]
        );

        ResponseEntity<Map<String, Object>> response = imageController.uploadImage(emptyFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).isEqualTo("파일이 비어있습니다.");

        verify(imageUploadUseCase, never()).uploadAndProcess(any());
    }

    @Test
    @DisplayName("이미지 업로드 실패 - 예외 발생")
    void uploadImage_Exception() {
        when(imageUploadUseCase.uploadAndProcess(any(MultipartFile.class)))
                .thenThrow(new RuntimeException("Processing failed"));

        ResponseEntity<Map<String, Object>> response = imageController.uploadImage(mockFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(false);
        assertThat(response.getBody().get("message")).toString().contains("처리 실패");
    }

    @Test
    @DisplayName("최근 이미지와 비교 성공")
    void compareWithLatest_Success() {
        ImageComparison comparison = ImageComparison.builder()
                .currentImageId(2L)
                .previousImageId(1L)
                .similarityScore(0.85)
                .comparisonType(ImageComparison.ComparisonType.FACE_SIMILARITY)
                .details("Face similarity: 0.85, Text similarity: 0.65")
                .build();

        when(imageCompareUseCase.compareWithLatest(2L)).thenReturn(comparison);

        ResponseEntity<Map<String, Object>> response = imageController.compareWithLatest(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("currentImageId")).isEqualTo(2L);
        assertThat(data.get("previousImageId")).isEqualTo(1L);
        assertThat(data.get("similarityScore")).isEqualTo(0.85);

        verify(imageCompareUseCase).compareWithLatest(2L);
    }

    @Test
    @DisplayName("최근 이미지와 비교 실패 - 예외 발생")
    void compareWithLatest_Exception() {
        when(imageCompareUseCase.compareWithLatest(anyLong()))
                .thenThrow(new RuntimeException("Comparison failed"));

        ResponseEntity<Map<String, Object>> response = imageController.compareWithLatest(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    @DisplayName("두 이미지 직접 비교 성공")
    void compareImages_Success() {
        ImageComparison comparison = ImageComparison.builder()
                .currentImageId(1L)
                .previousImageId(2L)
                .similarityScore(0.75)
                .comparisonType(ImageComparison.ComparisonType.OCR_TEXT_SIMILARITY)
                .details("Text similarity: 0.75")
                .build();

        when(imageCompareUseCase.compareImages(1L, 2L)).thenReturn(comparison);

        ResponseEntity<Map<String, Object>> response = imageController.compareImages(1L, 2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("similarityScore")).isEqualTo(0.75);

        verify(imageCompareUseCase).compareImages(1L, 2L);
    }
}