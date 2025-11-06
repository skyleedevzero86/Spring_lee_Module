package com.sleekydz86.ocrstudy1.domain.model;

import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DocumentType 도메인 모델 테스트")
class DocumentTypeTest {

    @Test
    @DisplayName("DocumentType enum 값 확인")
    void documentType_EnumValues() {
        assertThat(DocumentType.RECEIPT.getDescription()).isEqualTo("영수증");
        assertThat(DocumentType.ID_CARD.getDescription()).isEqualTo("신분증");
        assertThat(DocumentType.DRIVER_LICENSE.getDescription()).isEqualTo("운전면허증");
        assertThat(DocumentType.PASSPORT.getDescription()).isEqualTo("여권");
        assertThat(DocumentType.INVOICE.getDescription()).isEqualTo("세금계산서");
        assertThat(DocumentType.CONTRACT.getDescription()).isEqualTo("계약서");
        assertThat(DocumentType.CERTIFICATE.getDescription()).isEqualTo("증명서");
        assertThat(DocumentType.ETC.getDescription()).isEqualTo("기타");
    }

    @Test
    @DisplayName("모든 DocumentType에 description 존재")
    void documentType_AllHaveDescription() {
        for (DocumentType type : DocumentType.values()) {
            assertThat(type.getDescription()).isNotBlank();
        }
    }
}

