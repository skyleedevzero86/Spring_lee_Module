package com.sleekydz86.ocrstudy1.infrastructure.adapter.out.ocr;

import com.sleekydz86.ocrstudy1.application.port.out.DocumentTypeDetectionPort;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.regex.Pattern;

@Slf4j
@Component
public class DocumentTypeDetectionAdapter implements DocumentTypeDetectionPort {

    private static final Pattern RECEIPT_PATTERN = Pattern.compile(
            "영수증|매출전표|영업일보|거래일시|결제|카드|현금|포인트|할인|부가세|합계|판매자"
    );

    private static final Pattern INVOICE_PATTERN = Pattern.compile(
            "세금계산서|계산서|공급가액|부가세액|합계금액|공급받는자|공급자|사업자번호"
    );

    private static final Pattern CONTRACT_PATTERN = Pattern.compile(
            "계약서|계약기간|계약금액|갑을|당사자|계약조건|서명|날인"
    );

    private static final Pattern CERTIFICATE_PATTERN = Pattern.compile(
            "증명서|발급일|유효기간|발급기관|증명|확인서"
    );

    @Override
    public DocumentType detectDocumentType(InputStream imageStream, String ocrText, String filename) {
        if (ocrText == null || ocrText.isEmpty()) {
            return DocumentType.ETC;
        }

        String lowerText = ocrText.toLowerCase();
        String filenameLower = filename != null ? filename.toLowerCase() : "";

        if (lowerText.contains("주민등록증") ||
                lowerText.contains("주민번호") ||
                lowerText.matches(".*\\d{6}-\\d{7}.*")) {
            return DocumentType.ID_CARD;
        }

        if (lowerText.contains("운전면허") ||
                lowerText.contains("면허번호") ||
                lowerText.contains("면허종류")) {
            return DocumentType.DRIVER_LICENSE;
        }

        if (lowerText.contains("여권") ||
                lowerText.contains("passport") ||
                lowerText.contains("passeport")) {
            return DocumentType.PASSPORT;
        }

        if (RECEIPT_PATTERN.matcher(lowerText).find() ||
                filenameLower.contains("receipt") ||
                filenameLower.contains("영수증") ||
                (lowerText.contains("판매") && lowerText.contains("금액"))) {
            return DocumentType.RECEIPT;
        }

        if (INVOICE_PATTERN.matcher(lowerText).find() ||
                filenameLower.contains("invoice") ||
                filenameLower.contains("세금계산서")) {
            return DocumentType.INVOICE;
        }

        if (CONTRACT_PATTERN.matcher(lowerText).find() ||
                filenameLower.contains("contract") ||
                filenameLower.contains("계약서")) {
            return DocumentType.CONTRACT;
        }

        if (CERTIFICATE_PATTERN.matcher(lowerText).find() ||
                filenameLower.contains("certificate") ||
                filenameLower.contains("증명서")) {
            return DocumentType.CERTIFICATE;
        }

        log.info("Document type not detected, defaulting to ETC. Filename: {}", filename);
        return DocumentType.ETC;
    }
}