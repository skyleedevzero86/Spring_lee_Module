package com.sleekydz86.ocrstudy1.application.service;

import com.sleekydz86.ocrstudy1.application.port.out.FaceRecognitionPort;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.model.ImageComparison;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageCompareService 테스트")
class ImageCompareServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private FaceRecognitionPort faceRecognitionPort;

    @InjectMocks
    private ImageCompareService imageCompareService;

    private Image image1;
    private Image image2;

    @BeforeEach
    void setUp() {
        image1 = Image.builder()
                .id(1L)
                .originalFilename("image1.jpg")
                .faceEncoding("face_encoding_1")
                .ocrText("테스트 텍스트 1")
                .build();

        image2 = Image.builder()
                .id(2L)
                .originalFilename("image2.jpg")
                .faceEncoding("face_encoding_2")
                .ocrText("테스트 텍스트 2")
                .build();
    }

    @Test
    @DisplayName("최근 이미지와 비교 - 얼굴 유사도 계산")
    void compareWithLatest_FaceSimilarity() {
        Image latestImage = Image.builder()
                .id(3L)
                .faceEncoding("face_encoding_latest")
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image1));
        when(imageRepository.findLatest()).thenReturn(Optional.of(latestImage));
        when(faceRecognitionPort.compareFaces(anyString(), anyString())).thenReturn(0.85);

        ImageComparison result = imageCompareService.compareWithLatest(1L);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentImageId()).isEqualTo(1L);
        assertThat(result.getPreviousImageId()).isEqualTo(3L);
        assertThat(result.getSimilarityScore()).isEqualTo(0.85);
        assertThat(result.getComparisonType()).isEqualTo(ImageComparison.ComparisonType.FACE_SIMILARITY);
        verify(faceRecognitionPort).compareFaces("face_encoding_1", "face_encoding_latest");
    }

    @Test
    @DisplayName("최근 이미지와 비교 - OCR 텍스트 유사도 계산")
    void compareWithLatest_TextSimilarity() {
        Image imageWithoutFace = Image.builder()
                .id(1L)
                .ocrText("같은 텍스트 내용")
                .build();

        Image latestImage = Image.builder()
                .id(2L)
                .ocrText("같은 텍스트 내용")
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(imageWithoutFace));
        when(imageRepository.findLatest()).thenReturn(Optional.of(latestImage));

        ImageComparison result = imageCompareService.compareWithLatest(1L);

        assertThat(result).isNotNull();
        assertThat(result.getSimilarityScore()).isGreaterThan(0.0);
        assertThat(result.getComparisonType()).isEqualTo(ImageComparison.ComparisonType.OCR_TEXT_SIMILARITY);
    }

    @Test
    @DisplayName("최근 이미지와 비교 - 비교할 이미지 없음")
    void compareWithLatest_NoPreviousImage() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image1));
        when(imageRepository.findLatest()).thenReturn(Optional.empty());

        ImageComparison result = imageCompareService.compareWithLatest(1L);

        assertThat(result).isNotNull();
        assertThat(result.getSimilarityScore()).isEqualTo(0.0);
        assertThat(result.getDetails()).contains("No previous image to compare");
    }

    @Test
    @DisplayName("두 이미지 직접 비교 - 성공")
    void compareImages_Success() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image1));
        when(imageRepository.findById(2L)).thenReturn(Optional.of(image2));
        when(faceRecognitionPort.compareFaces(anyString(), anyString())).thenReturn(0.75);

        ImageComparison result = imageCompareService.compareImages(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentImageId()).isEqualTo(1L);
        assertThat(result.getPreviousImageId()).isEqualTo(2L);
        assertThat(result.getSimilarityScore()).isEqualTo(0.75);
    }

    @Test
    @DisplayName("두 이미지 비교 - 첫 번째 이미지 없음")
    void compareImages_FirstImageNotFound() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageCompareService.compareImages(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image not found: 1");
    }

    @Test
    @DisplayName("두 이미지 비교 - 두 번째 이미지 없음")
    void compareImages_SecondImageNotFound() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image1));
        when(imageRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imageCompareService.compareImages(1L, 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image not found: 2");
    }

    @Test
    @DisplayName("이미지 비교 - 얼굴 인코딩이 없는 경우 텍스트 유사도 사용")
    void compareImages_NoFaceEncoding_UsesTextSimilarity() {
        Image imageWithoutFace1 = Image.builder()
                .id(1L)
                .ocrText("문서 내용 1")
                .build();

        Image imageWithoutFace2 = Image.builder()
                .id(2L)
                .ocrText("문서 내용 1")
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(imageWithoutFace1));
        when(imageRepository.findById(2L)).thenReturn(Optional.of(imageWithoutFace2));

        ImageComparison result = imageCompareService.compareImages(1L, 2L);

        assertThat(result).isNotNull();
        assertThat(result.getComparisonType()).isEqualTo(ImageComparison.ComparisonType.OCR_TEXT_SIMILARITY);
        verify(faceRecognitionPort, never()).compareFaces(anyString(), anyString());
    }

    @Test
    @DisplayName("이미지 비교 캐싱 테스트 - 같은 이미지 쌍 비교")
    void compareImages_CachingTest() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image1));
        when(imageRepository.findById(2L)).thenReturn(Optional.of(image2));
        when(faceRecognitionPort.compareFaces(anyString(), anyString())).thenReturn(0.75);

        ImageComparison result1 = imageCompareService.compareImages(1L, 2L);

        ImageComparison result2 = imageCompareService.compareImages(1L, 2L);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getSimilarityScore()).isEqualTo(result2.getSimilarityScore());
    }

    @Test
    @DisplayName("최근 이미지 비교 캐싱 테스트")
    void compareWithLatest_CachingTest() {
        Image latestImage = Image.builder()
                .id(3L)
                .faceEncoding("face_encoding_latest")
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image1));
        when(imageRepository.findLatest()).thenReturn(Optional.of(latestImage));
        when(faceRecognitionPort.compareFaces(anyString(), anyString())).thenReturn(0.85);

        ImageComparison result1 = imageCompareService.compareWithLatest(1L);
        ImageComparison result2 = imageCompareService.compareWithLatest(1L);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getSimilarityScore()).isEqualTo(result2.getSimilarityScore());
    }
}

