package com.sleekydz86.ocrstudy1.infrastructure.adapter.in.web;

import com.sleekydz86.ocrstudy1.application.port.in.IdVerificationUseCase;
import com.sleekydz86.ocrstudy1.doamin.model.IdVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdVerificationController 테스트")
class IdVerificationControllerTest {

    @Mock
    private IdVerificationUseCase idVerificationUseCase;

    @InjectMocks
    private IdVerificationController controller;

    private IdVerification idVerification;

    @BeforeEach
    void setUp() {
        IdVerification.ExtractedInfo extractedInfo = IdVerification.ExtractedInfo.builder()
                .name("홍길동")
                .idNumber("900101-1234567")
                .dateOfBirth("1990-01-01")
                .address("서울시 강남구")
                .build();

        idVerification = IdVerification.builder()
                .isIdCard(true)
                .documentType("national_id")
                .extractedInfo(extractedInfo)
                .verificationConfidence(0.85)
                .build();
    }

    @Test
    @DisplayName("신분증 인증 성공")
    void verifyIdCard_Success() {
        when(idVerificationUseCase.verifyIdCard(1L)).thenReturn(idVerification);

        ResponseEntity<Map<String, Object>> response = controller.verifyIdCard(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("success")).isEqualTo(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        assertThat(data.get("isIdCard")).isEqualTo(true);
        assertThat(data.get("documentType")).isEqualTo("national_id");
        assertThat(data.get("verificationConfidence")).isEqualTo(0.85);

        @SuppressWarnings("unchecked")
        Map<String, Object> extractedInfo = (Map<String, Object>) data.get("extractedInfo");
        assertThat(extractedInfo.get("name")).isEqualTo("홍길동");
        assertThat(extractedInfo.get("idNumber")).isEqualTo("900101-1234567");

        verify(idVerificationUseCase).verifyIdCard(1L);
    }

    @Test
    @DisplayName("신분증 인증 실패 - 이미지 없음")
    void verifyIdCard_ImageNotFound() {
        when(idVerificationUseCase.verifyIdCard(1L))
                .thenThrow(new IllegalArgumentException("Image not found: 1"));

        ResponseEntity<Map<String, Object>> response = controller.verifyIdCard(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    @DisplayName("신분증 인증 실패 - 일반 예외")
    void verifyIdCard_GeneralException() {
        when(idVerificationUseCase.verifyIdCard(1L))
                .thenThrow(new RuntimeException("Verification error"));

        ResponseEntity<Map<String, Object>> response = controller.verifyIdCard(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("success")).isEqualTo(false);
    }

    @Test
    @DisplayName("신분증 인증 - 추출된 정보 없음")
    void verifyIdCard_NoExtractedInfo() {
        IdVerification verificationWithoutInfo = IdVerification.builder()
                .isIdCard(true)
                .documentType("national_id")
                .extractedInfo(null)
                .verificationConfidence(0.8)
                .build();

        when(idVerificationUseCase.verifyIdCard(1L)).thenReturn(verificationWithoutInfo);

        ResponseEntity<Map<String, Object>> response = controller.verifyIdCard(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        @SuppressWarnings("unchecked")
        Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
        @SuppressWarnings("unchecked")
        Map<String, Object> extractedInfo = (Map<String, Object>) data.get("extractedInfo");
        assertThat(extractedInfo.get("name")).isNull();
    }
}