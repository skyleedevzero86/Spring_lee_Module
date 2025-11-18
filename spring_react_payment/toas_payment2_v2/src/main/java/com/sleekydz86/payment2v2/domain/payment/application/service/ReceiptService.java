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
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReceiptService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 20;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float HEADER_FONT_SIZE = 12;
    private static final float BODY_FONT_SIZE = 10;
    private static final float QR_CODE_SIZE = 100;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd. HH:mm:ss");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd.");

    private PDType0Font koreanFont = null;
    private PDType0Font koreanBoldFont = null;

    private PDType0Font loadKoreanFont(PDDocument document, boolean bold) {
        if (bold && koreanBoldFont != null) {
            return koreanBoldFont;
        }
        if (!bold && koreanFont != null) {
            return koreanFont;
        }

        try {
            String osName = System.getProperty("os.name").toLowerCase();
            Path fontPath = null;

            if (osName.contains("win")) {
                String windowsFontDir = System.getenv("WINDIR");
                if (windowsFontDir == null) {
                    windowsFontDir = "C:\\Windows";
                }
                Path fontsDir = Paths.get(windowsFontDir, "Fonts");

                String[] fontFiles = bold
                    ? new String[]{"malgunbd.ttf", "NanumGothicBold.ttf", "gulim.ttc"}
                    : new String[]{"malgun.ttf", "NanumGothic.ttf", "gulim.ttc"};

                for (String fontFile : fontFiles) {
                    Path path = fontsDir.resolve(fontFile);
                    if (Files.exists(path)) {
                        fontPath = path;
                        break;
                    }
                }
            } else if (osName.contains("mac")) {
                String[] fontPaths = bold
                    ? new String[]{"/System/Library/Fonts/AppleGothic.ttf", "/Library/Fonts/AppleGothic.ttf"}
                    : new String[]{"/System/Library/Fonts/AppleGothic.ttf", "/Library/Fonts/AppleGothic.ttf"};

                for (String pathStr : fontPaths) {
                    Path path = Paths.get(pathStr);
                    if (Files.exists(path)) {
                        fontPath = path;
                        break;
                    }
                }
            } else {
                String[] fontPaths = {
                    "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc",
                    "/usr/share/fonts/truetype/nanum/NanumGothic.ttf"
                };

                for (String pathStr : fontPaths) {
                    Path path = Paths.get(pathStr);
                    if (Files.exists(path)) {
                        fontPath = path;
                        break;
                    }
                }
            }

            if (fontPath != null && Files.exists(fontPath)) {
                PDType0Font font = PDType0Font.load(document, fontPath.toFile());
                if (bold) {
                    koreanBoldFont = font;
                } else {
                    koreanFont = font;
                }
                log.info("한글 폰트 로드 성공: {}", fontPath);
                return font;
            }
        } catch (Exception e) {
            log.warn("한글 폰트 로드 실패: {}", e.getMessage());
        }

        return null;
    }

    private boolean containsKorean(String text) {
        if (text == null) {
            return false;
        }
        return text.chars().anyMatch(ch -> ch >= 0xAC00 && ch <= 0xD7A3);
    }

    public byte[] generateReceiptPdf(PaymentDetailResponse paymentDetail, Long paymentId) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font koreanBoldFont = loadKoreanFont(document, true);
            PDType0Font koreanFont = loadKoreanFont(document, false);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PDRectangle.A4.getHeight() - MARGIN;
                float pageWidth = PDRectangle.A4.getWidth();
                float contentWidth = pageWidth - (MARGIN * 2);

                contentStream.beginText();
                if (koreanBoldFont != null) {
                    contentStream.setFont(koreanBoldFont, TITLE_FONT_SIZE);
                } else {
                    contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), TITLE_FONT_SIZE);
                }
                float titleWidth = TITLE_FONT_SIZE * 6;
                contentStream.newLineAtOffset((pageWidth - titleWidth) / 2, yPosition);
                contentStream.showText("결제 영수증");
                contentStream.endText();
                yPosition -= (LINE_HEIGHT * 1.5f);

                drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN);
                yPosition -= (LINE_HEIGHT * 2);

                float qrX = pageWidth - MARGIN - QR_CODE_SIZE;
                float qrY = yPosition - QR_CODE_SIZE;
                
                if (paymentId != null) {
                    try {
                        String qrUrl = frontendUrl + "/payments/" + paymentId;
                        BufferedImage qrImage = generateQRCode(qrUrl, (int) QR_CODE_SIZE);
                        PDImageXObject qrImageXObject = PDImageXObject.createFromByteArray(document, 
                            imageToByteArray(qrImage), "qr-code");
                        contentStream.drawImage(qrImageXObject, qrX, qrY, QR_CODE_SIZE, QR_CODE_SIZE);
                    } catch (Exception e) {
                        log.warn("QR 코드 생성 실패: {}", e.getMessage());
                    }
                }

                yPosition = addSection(contentStream, "주문 정보", yPosition, pageWidth, contentWidth, document);
                yPosition = addKeyValue(contentStream, "주문번호", paymentDetail.getOrderNo(), yPosition, contentWidth, document);
                
                yPosition = addKeyValue(contentStream, "상품 설명", 
                    paymentDetail.getProductDesc() != null ? paymentDetail.getProductDesc() : "-", 
                    yPosition, contentWidth, document);
                yPosition = addKeyValue(contentStream, "상태", "결제 완료", yPosition, contentWidth, document);
                yPosition -= LINE_HEIGHT;

                yPosition = addSection(contentStream, "결제 정보", yPosition, pageWidth, contentWidth, document);
                
                if (paymentDetail.getPayMethod() != null && !paymentDetail.getPayMethod().isEmpty()) {
                    yPosition = addKeyValue(contentStream, "결제 수단", paymentDetail.getPayMethod(), yPosition, contentWidth, document);
                }
                
                yPosition = addKeyValue(contentStream, "결제 금액", 
                    formatAmount(paymentDetail.getAmount()) + "원", yPosition, contentWidth, document);
                
                if (paymentDetail.getAmountTaxFree() != null && paymentDetail.getAmountTaxFree().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "비과세 금액", 
                        formatAmount(paymentDetail.getAmountTaxFree()) + "원", yPosition, contentWidth, document);
                }
                
                if (paymentDetail.getAmountTaxable() != null && paymentDetail.getAmountTaxable().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "과세 금액", 
                        formatAmount(paymentDetail.getAmountTaxable()) + "원", yPosition, contentWidth, document);
                }
                
                if (paymentDetail.getAmountVat() != null && paymentDetail.getAmountVat().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "부가세", 
                        formatAmount(paymentDetail.getAmountVat()) + "원", yPosition, contentWidth, document);
                }
                
                if (paymentDetail.getPaidAmount() != null && paymentDetail.getPaidAmount().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "실제 결제 금액", 
                        formatAmount(paymentDetail.getPaidAmount()) + "원", yPosition, contentWidth, document);
                }
                
                if (paymentDetail.getDiscountedAmount() != null && paymentDetail.getDiscountedAmount().compareTo(BigDecimal.ZERO) > 0) {
                    yPosition = addKeyValue(contentStream, "할인 금액", 
                        formatAmount(paymentDetail.getDiscountedAmount()) + "원", yPosition, contentWidth, document);
                }
                
                if (paymentDetail.getPaidTs() != null && !paymentDetail.getPaidTs().isEmpty()) {
                    yPosition = addKeyValue(contentStream, "결제 완료일", paymentDetail.getPaidTs(), yPosition, contentWidth, document);
                } else if (paymentDetail.getApprovalTime() != null && !paymentDetail.getApprovalTime().isEmpty()) {
                    yPosition = addKeyValue(contentStream, "승인 시간", paymentDetail.getApprovalTime(), yPosition, contentWidth, document);
                }
                
                if (paymentDetail.getExpiredTime() != null) {
                    yPosition = addKeyValue(contentStream, "만료일", 
                        formatDateTime(paymentDetail.getExpiredTime()), yPosition, contentWidth, document);
                }
                
                yPosition -= LINE_HEIGHT;

                if (paymentDetail.getCard() != null) {
                    CardInfo card = paymentDetail.getCard();
                    yPosition = addSection(contentStream, "카드 정보", yPosition, pageWidth, contentWidth, document);
                    
                    if (card.getCardCompanyName() != null && !card.getCardCompanyName().isEmpty()) {
                        yPosition = addKeyValue(contentStream, "카드사", card.getCardCompanyName(), yPosition, contentWidth, document);
                    }
                    
                    if (card.getCardNumber() != null && !card.getCardNumber().isEmpty()) {
                        String maskedCardNumber = maskCardNumber(card.getCardNumber());
                        yPosition = addKeyValue(contentStream, "카드번호", maskedCardNumber, yPosition, contentWidth, document);
                    } else if (card.getCardNum4Print() != null && !card.getCardNum4Print().isEmpty()) {
                        yPosition = addKeyValue(contentStream, "카드번호", card.getCardNum4Print(), yPosition, contentWidth, document);
                    }
                    
                    if (card.getSpreadOut() != null && card.getSpreadOut() > 0) {
                        yPosition = addKeyValue(contentStream, "할부", 
                            card.getSpreadOut() + "개월", yPosition, contentWidth, document);
                    }
                    
                    yPosition -= LINE_HEIGHT;
                }

                if (paymentDetail.getAccountBankName() != null && !paymentDetail.getAccountBankName().isEmpty()) {
                    yPosition = addSection(contentStream, "계좌 정보", yPosition, pageWidth, contentWidth, document);
                    yPosition = addKeyValue(contentStream, "은행", paymentDetail.getAccountBankName(), yPosition, contentWidth, document);
                    
                    if (paymentDetail.getAccountNumber() != null && !paymentDetail.getAccountNumber().isEmpty()) {
                        yPosition = addKeyValue(contentStream, "계좌번호", 
                            maskAccountNumber(paymentDetail.getAccountNumber()), yPosition, contentWidth, document);
                    }
                    
                    yPosition -= LINE_HEIGHT;
                }
                
                yPosition -= (LINE_HEIGHT * 2);

                drawLine(contentStream, MARGIN, yPosition, pageWidth - MARGIN);
                yPosition -= LINE_HEIGHT;

                if (koreanFont != null) {
                    contentStream.beginText();
                    contentStream.setFont(koreanFont, 8);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("이 영수증은 결제 내역 확인용으로 사용됩니다.");
                    contentStream.endText();
                    yPosition -= LINE_HEIGHT;
                    
                    contentStream.beginText();
                    contentStream.setFont(koreanFont, 8);
                    contentStream.newLineAtOffset(MARGIN, yPosition);
                    contentStream.showText("문의사항이 있으시면 고객센터로 연락해주세요.");
                    contentStream.endText();
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private float addSection(PDPageContentStream contentStream, String title, float yPosition, 
                             float pageWidth, float contentWidth, PDDocument document) throws IOException {
        if (yPosition < MARGIN + 100) {
            return yPosition;
        }
        
        contentStream.beginText();
        PDType0Font koreanBoldFont = loadKoreanFont(document, true);
        if (koreanBoldFont != null && containsKorean(title)) {
            contentStream.setFont(koreanBoldFont, HEADER_FONT_SIZE);
        } else {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), HEADER_FONT_SIZE);
        }
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        yPosition -= (LINE_HEIGHT * 0.8f);
        
        drawLine(contentStream, MARGIN, yPosition, MARGIN + 100);
        yPosition -= LINE_HEIGHT;
        
        return yPosition;
    }

    private float addKeyValue(PDPageContentStream contentStream, String key, String value, 
                              float yPosition, float contentWidth, PDDocument document) throws IOException {
        if (yPosition < MARGIN + 50) {
            return yPosition;
        }
        
        PDType0Font koreanBoldFont = loadKoreanFont(document, true);
        PDType0Font koreanFont = loadKoreanFont(document, false);
        
        contentStream.beginText();
        if (koreanBoldFont != null && containsKorean(key)) {
            contentStream.setFont(koreanBoldFont, BODY_FONT_SIZE);
        } else {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), BODY_FONT_SIZE);
        }
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(key + ":");
        contentStream.endText();
        
        float valueX = MARGIN + 120;
        
        contentStream.beginText();
        if (koreanFont != null && containsKorean(value)) {
            contentStream.setFont(koreanFont, BODY_FONT_SIZE);
        } else {
            contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), BODY_FONT_SIZE);
        }
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

    private BufferedImage generateQRCode(String text, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size, hints);

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }
        return image;
    }

    private byte[] imageToByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}

