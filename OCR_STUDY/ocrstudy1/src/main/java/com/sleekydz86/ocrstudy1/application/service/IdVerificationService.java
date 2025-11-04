package com.sleekydz86.ocrstudy1.application.service;

import com.sleekydz86.ocrstudy1.application.port.in.IdVerificationUseCase;
import com.sleekydz86.ocrstudy1.application.port.out.OcrServicePort;
import com.sleekydz86.ocrstudy1.application.port.out.StoragePort;
import com.sleekydz86.ocrstudy1.doamin.model.IdVerification;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdVerificationService implements IdVerificationUseCase {

    private final ImageRepository imageRepository;
    private final StoragePort storagePort;
    private final OcrServicePort ocrServicePort;
    private static final Pattern KOREAN_ID_PATTERN = Pattern.compile("\\d{6}-\\d{7}");
    private static final Pattern DRIVER_LICENSE_PATTERN = Pattern.compile("운전면허|면허");
    private static final Pattern PASSPORT_PATTERN = Pattern.compile("PASSPORT|여권");

    @Override
    @Cacheable(value = "documentAnalysis", key = "'verifyIdCard:' + #imageId")
    public IdVerification verifyIdCard(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        try {
            InputStream imageStream = storagePort.downloadFile(image.getMinioObjectName());

            String ocrText = image.getOcrText();
            if (ocrText == null || ocrText.isEmpty()) {
                var ocrResult = ocrServicePort.extractText(imageStream, image.getOriginalFilename());
                ocrText = ocrResult != null ? ocrResult.getText() : "";
            }

            boolean isIdCard = detectIdCard(ocrText);
            String documentType = determineDocumentType(ocrText);

            IdVerification.ExtractedInfo extractedInfo = null;
            if (isIdCard) {
                extractedInfo = extractIdInfo(ocrText, documentType);
            }

            return IdVerification.builder()
                    .isIdCard(isIdCard)
                    .documentType(documentType)
                    .extractedInfo(extractedInfo)
                    .verificationConfidence(isIdCard ? 0.8 : 0.0)
                    .build();

        } catch (Exception e) {
            log.error("ID verification failed for image: {}", imageId, e);
            return IdVerification.builder()
                    .isIdCard(false)
                    .documentType("unknown")
                    .verificationConfidence(0.0)
                    .build();
        }
    }

    private boolean detectIdCard(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) {
            return false;
        }

        String lowerText = ocrText.toLowerCase();
        return lowerText.contains("주민등록증") ||
                lowerText.contains("운전면허") ||
                lowerText.contains("여권") ||
                lowerText.contains("passport") ||
                KOREAN_ID_PATTERN.matcher(ocrText).find();
    }

    private String determineDocumentType(String ocrText) {
        if (ocrText == null || ocrText.isEmpty()) {
            return "unknown";
        }

        String lowerText = ocrText.toLowerCase();
        if (lowerText.contains("주민등록증")) {
            return "national_id";
        } else if (lowerText.contains("운전면허") || DRIVER_LICENSE_PATTERN.matcher(ocrText).find()) {
            return "driver_license";
        } else if (lowerText.contains("여권") || PASSPORT_PATTERN.matcher(ocrText).find()) {
            return "passport";
        }
        return "unknown";
    }

    private IdVerification.ExtractedInfo extractIdInfo(String ocrText, String documentType) {
        IdVerification.ExtractedInfo.ExtractedInfoBuilder builder = IdVerification.ExtractedInfo.builder();

        Matcher idMatcher = KOREAN_ID_PATTERN.matcher(ocrText);
        if (idMatcher.find()) {
            builder.idNumber(idMatcher.group());
        }

        Pattern namePattern = Pattern.compile("(?:이름|성명|NAME)[\\s:：]*([가-힣A-Z\\s]+)");
        Matcher nameMatcher = namePattern.matcher(ocrText);
        if (nameMatcher.find()) {
            builder.name(nameMatcher.group(1).trim());
        }

        Pattern birthPattern = Pattern.compile("(?:생년월일|생년|출생)[\\s:：]*(\\d{4}[.\\-]\\d{2}[.\\-]\\d{2})");
        Matcher birthMatcher = birthPattern.matcher(ocrText);
        if (birthMatcher.find()) {
            builder.dateOfBirth(birthMatcher.group(1));
        }

        Pattern addressPattern = Pattern.compile("(?:주소|거주지)[\\s:：]*([가-힣\\s0-9\\-]+)");
        Matcher addressMatcher = addressPattern.matcher(ocrText);
        if (addressMatcher.find()) {
            builder.address(addressMatcher.group(1).trim());
        }

        return builder.build();
    }
}

