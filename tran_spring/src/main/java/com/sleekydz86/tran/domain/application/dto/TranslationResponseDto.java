package com.sleekydz86.tran.domain.application.dto;

import com.sleekydz86.tran.domain.model.record.Translation;

import java.time.format.DateTimeFormatter;

public class TranslationResponseDto {
    private final String originalText;
    private final String translatedText;
    private final String detectedSourceLanguage;
    private final String detectedSourceLanguageName;
    private final String targetLanguage;
    private final String targetLanguageName;
    private final String translatedAt;
    private final boolean successful;

    private TranslationResponseDto(
            String originalText,
            String translatedText,
            String detectedSourceLanguage,
            String detectedSourceLanguageName,
            String targetLanguage,
            String targetLanguageName,
            String translatedAt,
            boolean successful
    ) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.detectedSourceLanguage = detectedSourceLanguage;
        this.detectedSourceLanguageName = detectedSourceLanguageName;
        this.targetLanguage = targetLanguage;
        this.targetLanguageName = targetLanguageName;
        this.translatedAt = translatedAt;
        this.successful = successful;
    }

    public static TranslationResponseDto from(Translation translation) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return new TranslationResponseDto(
                translation.getOriginalText(),
                translation.getTranslatedText(),
                translation.getDetectedSourceLanguage().getCode(),
                translation.getDetectedSourceLanguage().getDisplayName(),
                translation.getTargetLanguage().getCode(),
                translation.getTargetLanguage().getDisplayName(),
                translation.getTranslatedAt().format(formatter),
                translation.isSuccessful()
        );
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public String getDetectedSourceLanguage() {
        return detectedSourceLanguage;
    }

    public String getDetectedSourceLanguageName() {
        return detectedSourceLanguageName;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getTargetLanguageName() {
        return targetLanguageName;
    }

    public String getTranslatedAt() {
        return translatedAt;
    }

    public boolean isSuccessful() {
        return successful;
    }

    @Override
    public String toString() {
        return "TranslationResponseDto{" +
                "detectedSourceLanguage='" + detectedSourceLanguage + '\'' +
                ", targetLanguage='" + targetLanguage + '\'' +
                ", successful=" + successful +
                ", translatedAt='" + translatedAt + '\'' +
                '}';
    }
}
