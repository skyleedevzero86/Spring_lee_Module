package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.web;

import com.sleekydz86.ocrstudy1.application.port.in.DocumentAnalysisUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentAnalysisController 테스트")
class DocumentAnalysisControllerTest {

    @Mock
    private DocumentAnalysisUseCase documentAnalysisUseCase;

    @InjectMocks
    private DocumentAnalysisController controller;

    private DocumentAnalysis documentAnalysis;

    @BeforeEach
    void setUp() {
        documentAnalysis = DocumentAnalysis.builder()
                .imageId(1L)
                .summary("문서 요약")
                .authenticityScore(0.95)
                .isAuthentic(true)
                .detectedIssues(List.of())
                .extractedFields(Map.of("금액", "10000"))
                .aiInsights("정상 문서")
                .confidence(0.90)
                .build();
    }

    @Test
    @DisplayName("문서 분석 성공")
    void analyzeDocument_Success() {
        when(documentAnalysisUseCase.analyzeDocument(1L)).thenReturn(documentAnalysis);

        ResponseEntity<Map<String, Object>> response = controller.analyzeDocument(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("imageId")).isEqualTo(1L);
        assertThat(data.get("summary")).isEqualTo("문서 요약");
        assertThat(data.get("authenticityScore")).isEqualTo(0.95);

        verify(documentAnalysisUseCase).analyzeDocument(1L);
    }

    @Test
    @DisplayName("문서 분석 실패 - 이미지 없음")
    void analyzeDocument_ImageNotFound() {
        when(documentAnalysisUseCase.analyzeDocument(1L))
                .thenThrow(new IllegalArgumentException("Image not found: 1"));

        ResponseEntity<Map<String, Object>> response = controller.analyzeDocument(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    @DisplayName("문서 진위 검증 성공")
    void verifyDocument_Success() {
        DocumentAnalysis verification = DocumentAnalysis.builder()
                .imageId(1L)
                .isAuthentic(true)
                .authenticityScore(0.98)
                .detectedIssues(List.of())
                .aiInsights("진위 검증 통과")
                .confidence(0.95)
                .build();

        when(documentAnalysisUseCase.verifyDocument(1L)).thenReturn(verification);

        ResponseEntity<Map<String, Object>> response = controller.verifyDocument(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("isAuthentic")).isEqualTo(true);
        assertThat(data.get("authenticityScore")).isEqualTo(0.98);
    }

    @Test
    @DisplayName("문서 요약 성공")
    void summarizeDocument_Success() {
        DocumentAnalysis summary = DocumentAnalysis.builder()
                .imageId(1L)
                .summary("요약된 문서 내용입니다.")
                .confidence(0.85)
                .build();

        when(documentAnalysisUseCase.summarizeDocument(1L)).thenReturn(summary);

        ResponseEntity<Map<String, Object>> response = controller.summarizeDocument(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("summary")).isEqualTo("요약된 문서 내용입니다.");
    }

    @Test
    @DisplayName("문서 분석 실패 - 일반 예외")
    void analyzeDocument_GeneralException() {
        when(documentAnalysisUseCase.analyzeDocument(anyLong()))
                .thenThrow(new RuntimeException("Internal error"));

        ResponseEntity<Map<String, Object>> response = controller.analyzeDocument(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }
}

