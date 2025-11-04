package com.sleekydz86.ocrstudy1.application.service;

import com.sleekydz86.ocrstudy1.application.port.in.DocumentAnalysisUseCase;
import com.sleekydz86.ocrstudy1.application.port.out.AIAnalysisPort;
import com.sleekydz86.ocrstudy1.doamin.model.DocumentAnalysis;
import com.sleekydz86.ocrstudy1.doamin.model.Image;
import com.sleekydz86.ocrstudy1.doamin.repository.ImageRepository;
import com.sleekydz86.ocrstudy1.doamin.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentAnalysisService implements DocumentAnalysisUseCase {

    private final ImageRepository imageRepository;
    private final AIAnalysisPort aiAnalysisPort;
    private final EncryptionService encryptionService;

    @Override
    @Cacheable(value = "documentAnalysis", key = "'analyze:' + #imageId")
    public DocumentAnalysis analyzeDocument(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        String ocrText = getDecryptedOcrText(image);
        String documentType = image.getDocumentType() != null
                ? image.getDocumentType().getDescription()
                : "기타";

        DocumentAnalysis analysis = aiAnalysisPort.analyzeDocument(
                ocrText,
                documentType,
                image.getOriginalFilename()
        );

        analysis.setImageId(imageId);
        log.info("Document analysis completed for image: {}", imageId);

        return analysis;
    }

    @Override
    @Cacheable(value = "documentAnalysis", key = "'verify:' + #imageId")
    public DocumentAnalysis verifyDocument(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        String ocrText = getDecryptedOcrText(image);
        String documentType = image.getDocumentType() != null
                ? image.getDocumentType().getDescription()
                : "기타";

        Map<String, String> extractedInfo = new HashMap<>();
        if (image.getExtractedIdInfo() != null) {
            try {
                String decryptedInfo = encryptionService.decrypt(image.getEncryptedExtractedIdInfo());

                String[] lines = decryptedInfo.split("\n");
                for (String line : lines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            extractedInfo.put(parts[0].trim(), parts[1].trim());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to decrypt extracted info for verification", e);
            }
        }

        DocumentAnalysis verification = aiAnalysisPort.verifyDocument(
                ocrText,
                documentType,
                extractedInfo
        );

        verification.setImageId(imageId);
        log.info("Document verification completed for image: {}", imageId);

        return verification;
    }

    @Override
    @Cacheable(value = "documentAnalysis", key = "'summarize:' + #imageId")
    public DocumentAnalysis summarizeDocument(Long imageId) {
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageId));

        String ocrText = getDecryptedOcrText(image);
        String documentType = image.getDocumentType() != null
                ? image.getDocumentType().getDescription()
                : "기타";

        String summary = aiAnalysisPort.summarizeDocument(ocrText, documentType);

        DocumentAnalysis.DocumentAnalysisBuilder builder = DocumentAnalysis.builder();
        builder.imageId(imageId)
                .summary(summary)
                .authenticityScore(0.8)
                .isAuthentic(true)
                .detectedIssues(null)
                .extractedFields(null)
                .aiInsights("문서 요약이 생성되었습니다.")
                .confidence(0.85);

        log.info("Document summarization completed for image: {}", imageId);

        return builder.build();
    }

    private String getDecryptedOcrText(Image image) {
        if (image.getEncryptedOcrText() != null && !image.getEncryptedOcrText().isEmpty()) {
            try {
                return encryptionService.decrypt(image.getEncryptedOcrText());
            } catch (Exception e) {
                log.warn("Failed to decrypt OCR text, using plain text", e);
            }
        }
        return image.getOcrText() != null ? image.getOcrText() : "";
    }
}

