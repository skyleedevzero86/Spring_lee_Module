package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.ocr;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.ByteArrayInputStream;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentTypeDetectionAdapter 테스트")
class DocumentTypeDetectionAdapterTest {

    private DocumentTypeDetectionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DocumentTypeDetectionAdapter();
    }

    @Test
    @DisplayName("영수증 감지 성공")
    void detectDocumentType_Receipt() {
        String ocrText = "영수증 판매일: 2024-01-15 금액: 4,500원 결제";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "receipt.jpg");

        assertThat(result).isEqualTo(DocumentType.RECEIPT);
    }

    @Test
    @DisplayName("신분증 감지 성공")
    void detectDocumentType_IdCard() {
        String ocrText = "주민등록증 홍길동 900101-1234567";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "id_card.jpg");

        assertThat(result).isEqualTo(DocumentType.ID_CARD);
    }

    @Test
    @DisplayName("운전면허증 감지 성공")
    void detectDocumentType_DriverLicense() {
        String ocrText = "운전면허증 면허번호: 12-34-567890-12";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "driver_license.jpg");

        assertThat(result).isEqualTo(DocumentType.DRIVER_LICENSE);
    }

    @Test
    @DisplayName("여권 감지 성공")
    void detectDocumentType_Passport() {
        String ocrText = "여권 PASSPORT 국적: 대한민국";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "passport.jpg");

        assertThat(result).isEqualTo(DocumentType.PASSPORT);
    }

    @Test
    @DisplayName("세금계산서 감지 성공")
    void detectDocumentType_Invoice() {
        String ocrText = "세금계산서 공급가액: 100,000원 부가세액: 10,000원";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "invoice.jpg");

        assertThat(result).isEqualTo(DocumentType.INVOICE);
    }

    @Test
    @DisplayName("계약서 감지 성공")
    void detectDocumentType_Contract() {
        String ocrText = "계약서 계약기간: 2024-01-01 ~ 2024-12-31 당사자: 갑을";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "contract.jpg");

        assertThat(result).isEqualTo(DocumentType.CONTRACT);
    }

    @Test
    @DisplayName("증명서 감지 성공")
    void detectDocumentType_Certificate() {
        String ocrText = "증명서 발급일: 2024-01-15 발급기관: 서울시청";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "certificate.jpg");

        assertThat(result).isEqualTo(DocumentType.CERTIFICATE);
    }

    @Test
    @DisplayName("문서 타입 감지 실패 - 기본값 ETC")
    void detectDocumentType_Unknown_ReturnsEtc() {
        String ocrText = "일반 문서 내용";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "unknown.jpg");

        assertThat(result).isEqualTo(DocumentType.ETC);
    }

    @Test
    @DisplayName("빈 OCR 텍스트 - 기본값 ETC")
    void detectDocumentType_EmptyOcrText_ReturnsEtc() {
        String ocrText = "";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "test.jpg");

        assertThat(result).isEqualTo(DocumentType.ETC);
    }

    @Test
    @DisplayName("null OCR 텍스트 - 기본값 ETC")
    void detectDocumentType_NullOcrText_ReturnsEtc() {
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, null, "test.jpg");

        assertThat(result).isEqualTo(DocumentType.ETC);
    }

    @Test
    @DisplayName("파일명 기반 문서 타입 감지")
    void detectDocumentType_FilenameBased() {
        String ocrText = "일반 텍스트";
        var inputStream = new ByteArrayInputStream("test".getBytes());

        DocumentType result = adapter.detectDocumentType(inputStream, ocrText, "receipt_2024.jpg");

        assertThat(result).isEqualTo(DocumentType.RECEIPT);
    }
}