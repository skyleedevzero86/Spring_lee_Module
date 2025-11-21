package com.sleekydz86.tran.domain.model.record;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Translation {
    private final String originalText;
    private final String translatedText;
    private final Language detectedSourceLanguage;
    private final Language targetLanguage;
    private final LocalDateTime translatedAt;

    private Translation(
            String originalText,
            String translatedText,
            Language detectedSourceLanguage,
            Language targetLanguage,
            LocalDateTime translatedAt
    ) {
        this.originalText = originalText;
        this.translatedText = translatedText;
        this.detectedSourceLanguage = detectedSourceLanguage;
        this.targetLanguage = targetLanguage;
        this.translatedAt = translatedAt;
    }

    public static Translation of(
            String originalText,
            String translatedText,
            Language detectedSourceLanguage,
            Language targetLanguage
    ) {
        validateTranslation(originalText, translatedText, detectedSourceLanguage, targetLanguage);
        return new Translation(
                originalText,
                translatedText,
                detectedSourceLanguage,
                targetLanguage,
                LocalDateTime.now()
        );
    }

    private static void validateTranslation(
            String originalText,
            String translatedText,
            Language detectedSourceLanguage,
            Language targetLanguage
    ) {
        if (originalText == null || originalText.isBlank()) {
            throw new IllegalArgumentException("원본 텍스트는 필수입니다");
        }
        if (translatedText == null || translatedText.isBlank()) {
            throw new IllegalArgumentException("번역된 텍스트는 필수입니다");
        }
        if (detectedSourceLanguage == null) {
            throw new IllegalArgumentException("감지된 원본 언어는 필수입니다");
        }
        if (targetLanguage == null) {
            throw new IllegalArgumentException("목표 언어는 필수입니다");
        }
    }

    public String getOriginalText() {
        return originalText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public Language getDetectedSourceLanguage() {
        return detectedSourceLanguage;
    }

    public Language getTargetLanguage() {
        return targetLanguage;
    }

    public LocalDateTime getTranslatedAt() {
        return translatedAt;
    }

    public boolean isSuccessful() {
        return translatedText != null && !translatedText.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Translation that = (Translation) o;
        return Objects.equals(originalText, that.originalText) &&
                Objects.equals(translatedText, that.translatedText) &&
                Objects.equals(detectedSourceLanguage, that.detectedSourceLanguage) &&
                Objects.equals(targetLanguage, that.targetLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(originalText, translatedText, detectedSourceLanguage, targetLanguage);
    }

    @Override
    public String toString() {
        return "Translation{" +
                "detectedSourceLanguage=" + detectedSourceLanguage +
                ", targetLanguage=" + targetLanguage +
                ", originalLength=" + originalText.length() +
                ", translatedLength=" + translatedText.length() +
                ", translatedAt=" + translatedAt +
                '}';
    }
}
