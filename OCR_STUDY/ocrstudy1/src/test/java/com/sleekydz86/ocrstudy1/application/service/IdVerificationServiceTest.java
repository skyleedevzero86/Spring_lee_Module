package com.sleekydz86.ocrstudy1.application.service;

import com.sleekydz86.ocrstudy1.application.port.out.OcrServicePort;
import com.sleekydz86.ocrstudy1.application.port.out.StoragePort;
import com.sleekydz86.ocrstudy1.doamin.model.IdVerification;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.model.OcrResult;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdVerificationService 테스트")
class IdVerificationServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private StoragePort storagePort;

    @Mock
    private OcrServicePort ocrServicePort;

    @InjectMocks
    private IdVerificationService idVerificationService;

    private Image image;
    private OcrResult ocrResult;

    @BeforeEach
    void setUp() {
        image = Image.builder()
                .id(1L)
                .originalFilename("id_card.jpg")
                .minioObjectName("1234567890_id_card.jpg")
                .ocrText("주민등록증 홍길동 900101-1234567")
                .build();

        ocrResult = OcrResult.builder()
                .text("주민등록증 홍길동 900101-1234567")
                .confidence(0.95)
                .language("kor")
                .boundingBoxes(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("신분증 인증 성공 - 주민등록증")
    void verifyIdCard_Success_NationalId() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(storagePort.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(ocrServicePort.extractText(any(InputStream.class), anyString())).thenReturn(ocrResult);

        IdVerification result = idVerificationService.verifyIdCard(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsIdCard()).isTrue();
        assertThat(result.getDocumentType()).isEqualTo("national_id");
        assertThat(result.getVerificationConfidence()).isGreaterThan(0.0);
        assertThat(result.getExtractedInfo()).isNotNull();
        assertThat(result.getExtractedInfo().getIdNumber()).isEqualTo("900101-1234567");
    }

    @Test
    @DisplayName("신분증 인증 성공 - 운전면허증")
    void verifyIdCard_Success_DriverLicense() {
        image.setOcrText("운전면허증 홍길동");
        ocrResult = OcrResult.builder()
                .text("운전면허증 홍길동")
                .confidence(0.95)
                .language("kor")
                .boundingBoxes(new ArrayList<>())
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(storagePort.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(ocrServicePort.extractText(any(InputStream.class), anyString())).thenReturn(ocrResult);

        IdVerification result = idVerificationService.verifyIdCard(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsIdCard()).isTrue();
        assertThat(result.getDocumentType()).isEqualTo("driver_license");
    }

    @Test
    @DisplayName("신분증 인증 실패 - 이미지 없음")
    void verifyIdCard_ImageNotFound() {
        when(imageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> idVerificationService.verifyIdCard(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image not found: 1");
    }

    @Test
    @DisplayName("신분증 인증 실패 - 신분증 아님")
    void verifyIdCard_NotIdCard() {
        image.setOcrText("일반 문서 텍스트");
        ocrResult = OcrResult.builder()
                .text("일반 문서 텍스트")
                .confidence(0.90)
                .language("kor")
                .boundingBoxes(new ArrayList<>())
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(storagePort.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(ocrServicePort.extractText(any(InputStream.class), anyString())).thenReturn(ocrResult);

        IdVerification result = idVerificationService.verifyIdCard(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsIdCard()).isFalse();
        assertThat(result.getVerificationConfidence()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("신분증 정보 추출 - 이름 추출")
    void verifyIdCard_ExtractName() {
        image.setOcrText("이름: 홍길동 주민번호: 900101-1234567");
        ocrResult = OcrResult.builder()
                .text("이름: 홍길동 주민번호: 900101-1234567")
                .confidence(0.95)
                .language("kor")
                .boundingBoxes(new ArrayList<>())
                .build();

        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(storagePort.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(ocrServicePort.extractText(any(InputStream.class), anyString())).thenReturn(ocrResult);

        IdVerification result = idVerificationService.verifyIdCard(1L);

        assertThat(result.getExtractedInfo()).isNotNull();
        assertThat(result.getExtractedInfo().getName()).isNotBlank();
    }

    @Test
    @DisplayName("신분증 인증 - OCR 텍스트가 없을 때 OCR 재수행")
    void verifyIdCard_NoOcrText_ReExtract() {
        image.setOcrText(null);
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(storagePort.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(ocrServicePort.extractText(any(InputStream.class), anyString())).thenReturn(ocrResult);

        IdVerification result = idVerificationService.verifyIdCard(1L);

        assertThat(result).isNotNull();
        verify(ocrServicePort).extractText(any(InputStream.class), anyString());
    }

    @Test
    @DisplayName("신분증 인증 - 예외 발생 시 실패 처리")
    void verifyIdCard_Exception_Fallback() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(storagePort.downloadFile(anyString())).thenThrow(new RuntimeException("Storage error"));

        IdVerification result = idVerificationService.verifyIdCard(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsIdCard()).isFalse();
        assertThat(result.getDocumentType()).isEqualTo("unknown");
    }

    @Test
    @DisplayName("신분증 인증 캐싱 테스트 - 같은 이미지 두 번 검증")
    void verifyIdCard_CachingTest() {
        when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
        when(storagePort.downloadFile(anyString())).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(ocrServicePort.extractText(any(InputStream.class), anyString())).thenReturn(ocrResult);

        IdVerification result1 = idVerificationService.verifyIdCard(1L);
        IdVerification result2 = idVerificationService.verifyIdCard(1L);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getIsIdCard()).isEqualTo(result2.getIsIdCard());
        assertThat(result1.getDocumentType()).isEqualTo(result2.getDocumentType());
    }
}
