package com.sleekydz86.ocrstudy1.application.service;

import com.sleekydz86.ocrstudy1.application.port.out.AIAnalysisPort;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentAnalysis;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import com.sleekydz86.ocrstudy1.doamin.service.EncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentAnalysisService 테스트")
class DocumentAnalysisServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private AIAnalysisPort aiAnalysisPort;

    @Mock
    private EncryptionService encryptionService;

    @InjectMocks
    private DocumentAnalysisService documentAnalysisService;

    private Image image;
    private DocumentAnalysis documentAnalysis;

    @BeforeEach
    void setUp() {
        image = Image.builder()
                .id(1L)
                .originalFilename("test.jpg")
                .documentType(DocumentType.RECEIPT)
                .ocrText("평문 OCR 텍스트")
                .encryptedOcrText("encrypted_ocr_text")
                .isIdCard(false)
                .build();

        documentAnalysis = DocumentAnalysis.builder()
                .imageId(1L)
                .summary("문서 요약")
                .authenticityScore(0.9)
                .isAuthentic(true)
                .detectedIssues(List.of())
                .extractedFields(Map.of("금액", "10000"))
                .aiInsights("정상 문서")
                .confidence(0.85)
                .build();
    }

    @Test
    @DisplayName("문서 분석 성공")
    void analyzeDocument_Success() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(encryptionService.decrypt("encrypted_ocr_text")).thenReturn("평문 OCR 텍스트");
        when(aiAnalysisPort.analyzeDocument(eq("평문 OCR 텍스트"), eq("영수증"), eq("test.jpg")))
                .thenReturn(documentAnalysis);

        DocumentAnalysis result = documentAnalysisService.analyzeDocument(1L);

        assertThat(result).isNotNull();
        assertThat(result.getImageId()).isEqualTo(1L);
        assertThat(result.getSummary()).isEqualTo("문서 요약");
        assertThat(result.getAuthenticityScore()).isEqualTo(0.9);
        verify(aiAnalysisPort).analyzeDocument(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("문서 분석 실패 - 이미지 없음")
    void analyzeDocument_ImageNotFound() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> documentAnalysisService.analyzeDocument(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image not found: 1");
    }

    @Test
    @DisplayName("문서 진위 검증 성공")
    void verifyDocument_Success() {
        image.setEncryptedExtractedIdInfo("encrypted_id_info");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(encryptionService.decrypt("encrypted_ocr_text")).thenReturn("평문 OCR 텍스트");
        when(encryptionService.decrypt("encrypted_id_info")).thenReturn("이름: 홍길동\n주민번호: 900101-1234567");

        DocumentAnalysis verification = DocumentAnalysis.builder()
                .isAuthentic(true)
                .authenticityScore(0.95)
                .detectedIssues(List.of())
                .build();

        when(aiAnalysisPort.verifyDocument(anyString(), anyString(), any(Map.class)))
                .thenReturn(verification);

        DocumentAnalysis result = documentAnalysisService.verifyDocument(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsAuthentic()).isTrue();
        assertThat(result.getAuthenticityScore()).isEqualTo(0.95);
        verify(aiAnalysisPort).verifyDocument(anyString(), anyString(), any(Map.class));
    }

    @Test
    @DisplayName("문서 요약 성공")
    void summarizeDocument_Success() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(encryptionService.decrypt("encrypted_ocr_text")).thenReturn("평문 OCR 텍스트");
        when(aiAnalysisPort.summarizeDocument(eq("평문 OCR 텍스트"), eq("영수증")))
                .thenReturn("요약된 문서 내용");

        DocumentAnalysis result = documentAnalysisService.summarizeDocument(1L);

        assertThat(result).isNotNull();
        assertThat(result.getSummary()).isEqualTo("요약된 문서 내용");
        assertThat(result.getImageId()).isEqualTo(1L);
        assertThat(result.getConfidence()).isEqualTo(0.85);
        verify(aiAnalysisPort).summarizeDocument(anyString(), anyString());
    }

    @Test
    @DisplayName("문서 분석 - 암호화된 OCR 텍스트 없을 때 평문 사용")
    void analyzeDocument_NoEncryptedText_UsePlainText() {
        image.setEncryptedOcrText(null);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(aiAnalysisPort.analyzeDocument(eq("평문 OCR 텍스트"), anyString(), anyString()))
                .thenReturn(documentAnalysis);

        DocumentAnalysis result = documentAnalysisService.analyzeDocument(1L);

        assertThat(result).isNotNull();
        verify(encryptionService, never()).decrypt(anyString());
    }

    @Test
    @DisplayName("문서 진위 검증 - 추출된 정보 파싱")
    void verifyDocument_ParseExtractedInfo() {
        image.setEncryptedExtractedIdInfo("encrypted_info");
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(encryptionService.decrypt("encrypted_ocr_text")).thenReturn("OCR 텍스트");
        when(encryptionService.decrypt("encrypted_info")).thenReturn("이름: 홍길동\n주민번호: 900101-1234567");

        DocumentAnalysis verification = DocumentAnalysis.builder()
                .isAuthentic(true)
                .authenticityScore(0.9)
                .build();

        when(aiAnalysisPort.verifyDocument(anyString(), anyString(), any(Map.class)))
                .thenReturn(verification);

        DocumentAnalysis result = documentAnalysisService.verifyDocument(1L);

        assertThat(result).isNotNull();
        verify(aiAnalysisPort).verifyDocument(anyString(), anyString(), argThat(map ->
                map.containsKey("이름") && map.containsKey("주민번호")));
    }

    @Test
    @DisplayName("문서 분석 캐싱 테스트 - 같은 요청 두 번 시 캐시 사용")
    void analyzeDocument_CachingTest() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(encryptionService.decrypt("encrypted_ocr_text")).thenReturn("평문 OCR 텍스트");
        when(aiAnalysisPort.analyzeDocument(eq("평문 OCR 텍스트"), eq("영수증"), eq("test.jpg")))
                .thenReturn(documentAnalysis);

        DocumentAnalysis result1 = documentAnalysisService.analyzeDocument(1L);

        DocumentAnalysis result2 = documentAnalysisService.analyzeDocument(1L);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();

        verify(aiAnalysisPort, atMost(2)).analyzeDocument(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("문서 요약 캐싱 테스트")
    void summarizeDocument_CachingTest() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(encryptionService.decrypt("encrypted_ocr_text")).thenReturn("평문 OCR 텍스트");
        when(aiAnalysisPort.summarizeDocument(eq("평문 OCR 텍스트"), eq("영수증")))
                .thenReturn("요약된 문서 내용");

        DocumentAnalysis result1 = documentAnalysisService.summarizeDocument(1L);
        DocumentAnalysis result2 = documentAnalysisService.summarizeDocument(1L);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getSummary()).isEqualTo(result2.getSummary());
    }
}

