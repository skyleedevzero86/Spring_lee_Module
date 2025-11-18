package com.sleekydz86.payment2v2.domain.payment.application.service;

import com.sleekydz86.payment2v2.domain.payment.application.dto.CardInfo;
import com.sleekydz86.payment2v2.domain.payment.application.dto.PaymentDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 20;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float HEADER_FONT_SIZE = 12;
    private static final float BODY_FONT_SIZE = 10;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");

    public byte[] generateReceiptPdf(PaymentDetailResponse paymentDetail) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PDRectangle.A4.getHeight() - MARGIN;
                float pageWidth = PDRectangle.A4.getWidth();
                float contentWidth = pageWidth - (MARGIN * 2);


                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, TITLE_FONT_SIZE);
                float titleWidth = TITLE_FONT_SIZE * 6;
                contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, yPosition);
                contentStream.showText("결제 영수증");
                contentStream.endText();
                yPosition -= (LINE_HEIGHT * 1.5f);


                drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN);
                yPosition -= LINE_HEIGHT;


                yPosition = addSection(contentStream, "주문 정보", yPosition, pageWidth, contentWidth);
                yPosition = addKeyValue(contentStream, "주문번호", paymentDetail.getOrderNo(), yPosition, contentWidth);

                if (paymentDetail.getTransactionId() != null && !paymentDetail.getTransactionId().isEmpty()) {
                    yPosition = addKeyValue(contentStream, "거래 ID", paymentDetail.getTransactionId(), yPosition, contentWidth);
                }

                yPosition = addKeyValue(contentStream, "상품 설명",
                    paymentDetail.getProductDesc() != null ? paymentDetail.getProductDesc() : "-",
                    yPosition, contentWidth);
                yPosition = addKeyValue(contentStream, "상태", paymentDetail.getStatus(), yPosition, contentWidth);
                yPosition -= LINE_HEIGHT;


                yPosition = addSection(contentStream, "결제 정보", yPosition, pageWidth, contentWidth);

                if (paymentDetail.getPayMethod() != null && !paymentDetail.getPayMethod().isEmpty()) {
                    yPosition = addKeyValue(contentStream, "결제 수단", paymentDetail.getPayMethod(), yPosition, contentWidth);
                }

                yPosition = addKeyValue(contentStream, "결제 금액",
                    formatAmount(paymentDetail.getAmount()) + "원", yPosition, contentWidth);

                if (paymentDetail.getAmountTaxFree() != null && paymentDetail.getAmountTaxFree().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "비과세 금액",
                        formatAmount(paymentDetail.getAmountTaxFree()) + "원", yPosition, contentWidth);
                }

                if (paymentDetail.getAmountTaxable() != null && paymentDetail.getAmountTaxable().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "과세 금액",
                        formatAmount(paymentDetail.getAmountTaxable()) + "원", yPosition, contentWidth);
                }

                if (paymentDetail.getAmountVat() != null && paymentDetail.getAmountVat().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "부가세",
                        formatAmount(paymentDetail.getAmountVat()) + "원", yPosition, contentWidth);
                }

                if (paymentDetail.getPaidAmount() != null && paymentDetail.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "실제 결제 금액",
                        formatAmount(paymentDetail.getPaidAmount()) + "원", yPosition, contentWidth);
                }

                if (paymentDetail.getDiscountedAmount() != null && paymentDetail.getDiscountedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "할인 금액",
                        formatAmount(paymentDetail.getDiscountedAmount()) + "원", yPosition, contentWidth);
                }

                yPosition -= LINE_HEIGHT;


                if (paymentDetail.getCard() != null) {
                    CardInfo card = paymentDetail.getCard();
                    yPosition = addSection(contentStream, "카드 정보", yPosition, pageWidth, contentWidth);

                    if (card.getCardCompanyName() != null && !card.getCardCompanyName().isEmpty()) {
                        yPosition = addKeyValue(contentStream, "카드사", card.getCardCompanyName(), yPosition, contentWidth);
                    }

                    if (card.getCardNumber() != null && !card.getCardNumber().isEmpty()) {
                        String maskedCardNumber = maskCardNumber(card.getCardNumber());
                        yPosition = addKeyValue(contentStream, "카드번호", maskedCardNumber, yPosition, contentWidth);
                    } else if (card.getCardNum4Print() != null && !card.getCardNum4Print().isEmpty()) {
                        yPosition = addKeyValue(contentStream, "카드번호", card.getCardNum4Print(), yPosition, contentWidth);
                    }

                    if (card.getSpreadOut() != null && card.getSpreadOut() > 0) {
                        yPosition = addKeyValue(contentStream, "할부",
                            card.getSpreadOut() + "개월", yPosition, contentWidth);
                    }

                    yPosition -= LINE_HEIGHT;
                }


                if (paymentDetail.getAccountBankName() != null && !paymentDetail.getAccountBankName().isEmpty()) {
                    yPosition = addSection(contentStream, "계좌 정보", yPosition, pageWidth, contentWidth);
                    yPosition = addKeyValue(contentStream, "은행", paymentDetail.getAccountBankName(), yPosition, contentWidth);

                    if (paymentDetail.getAccountNumber() != null && !paymentDetail.getAccountNumber().isEmpty()) {
                        yPosition = addKeyValue(contentStream, "계좌번호",
                            maskAccountNumber(paymentDetail.getAccountNumber()), yPosition, contentWidth);
                    }

                    yPosition -= LINE_HEIGHT;
                }


                yPosition = addSection(contentStream, "날짜 정보", yPosition, pageWidth, contentWidth);

                if (paymentDetail.getCreatedAt() != null) {
                    yPosition = addKeyValue(contentStream, "생성일",
                        formatDateTime(paymentDetail.getCreatedAt()), yPosition, contentWidth);
                }

                if (paymentDetail.getPaidTs() != null && !paymentDetail.getPaidTs().isEmpty()) {
                    yPosition = addKeyValue(contentStream, "결제 완료일", paymentDetail.getPaidTs(), yPosition, contentWidth);
                } else if (paymentDetail.getApprovalTime() != null && !paymentDetail.getApprovalTime().isEmpty()) {
                    yPosition = addKeyValue(contentStream, "승인 시간", paymentDetail.getApprovalTime(), yPosition, contentWidth);
                }

                if (paymentDetail.getExpiredTime() != null) {
                    yPosition = addKeyValue(contentStream, "만료일",
                        formatDateTime(paymentDetail.getExpiredTime()), yPosition, contentWidth);
                }

                yPosition -= (LINE_HEIGHT * 2);


                drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN);
                yPosition -= LINE_HEIGHT;


                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("이 영수증은 결제 내역 확인용으로 사용됩니다.");
                contentStream.endText();
                yPosition -= LINE_HEIGHT;

                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 8);
                contentStream.newLineAtOffset(MARGIN, yPosition);
                contentStream.showText("문의사항이 있으시면 고객센터로 연락해주세요.");
                contentStream.endText();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private float addSection(PDPageContentStream contentStream, String title, float yPosition,
                             float pageWidth, float contentWidth) throws IOException {
        if (yPosition < MARGIN + 100) {
            return yPosition;
        }

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADER_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        yPosition -= (LINE_HEIGHT * 0.8f);

        drawLine(contentStream, MARGIN, yPosition, MARGIN + 100);
        yPosition -= LINE_HEIGHT;

        return yPosition;
    }

    private float addKeyValue(PDPageContentStream contentStream, String key, String value,
                              float yPosition, float contentWidth) throws IOException {
        if (yPosition < MARGIN + 50) {
            return yPosition;
        }

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(key + ":");
        contentStream.endText();


        float valueX = MARGIN + 120;
        float maxWidth = contentWidth - 120;

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(valueX, yPosition);


        String displayValue = value;
        if (displayValue.length() > 50) {
            displayValue = displayValue.substring(0, 47) + "...";
        }
        contentStream.showText(displayValue);
        contentStream.endText();

        return yPosition - LINE_HEIGHT;
    }

    private void drawLine(PDPageContentStream contentStream, float x1, float y, float x2) throws IOException {
        contentStream.moveTo(x1, y);
        contentStream.lineTo(x2, y);
        contentStream.stroke();
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) {
            return "0";
        }
        return String.format("%,d", amount.intValue());
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "-";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 8) {
            return cardNumber;
        }

        int length = cardNumber.length();
        if (length <= 8) {
            return cardNumber;
        }
        String prefix = cardNumber.substring(0, 4);
        String suffix = cardNumber.substring(length - 4);
        return prefix + "****" + suffix;
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return accountNumber;
        }

        int length = accountNumber.length();
        if (length <= 4) {
            return "****";
        }
        return "****" + accountNumber.substring(length - 4);
    }
}

