package com.sleekydz86.tran.domain.model.record;

import java.util.Objects;

public final class TranslationRequest {
    private final String sourceText;
    private final Language sourceLanguage;
    private final Language targetLanguage;

    private TranslationRequest(String sourceText, Language sourceLanguage, Language targetLanguage) {
        this.sourceText = sourceText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public static TranslationRequest of(String sourceText, Language sourceLanguage, Language targetLanguage) {
        validateSourceText(sourceText);
        validateLanguages(sourceLanguage, targetLanguage);
        return new TranslationRequest(sourceText, sourceLanguage, targetLanguage);
    }

    public static TranslationRequest autoDetect(String sourceText, Language targetLanguage) {
        validateSourceText(sourceText);
        Objects.requireNonNull(targetLanguage, "목표 언어는 필수입니다");
        return new TranslationRequest(sourceText, null, targetLanguage);
    }

    private static void validateSourceText(String sourceText) {
        if (sourceText == null || sourceText.isBlank()) {
            throw new IllegalArgumentException("번역할 텍스트는 필수입니다");
        }
        if (sourceText.length() > 5000) {
            throw new IllegalArgumentException("번역할 텍스트는 5000자를 초과할 수 없습니다");
        }
    }

    private static void validateLanguages(Language sourceLanguage, Language targetLanguage) {
        Objects.requireNonNull(sourceLanguage, "원본 언어는 필수입니다");
        Objects.requireNonNull(targetLanguage, "목표 언어는 필수입니다");

        if (sourceLanguage.equals(targetLanguage)) {
            throw new IllegalArgumentException("원본 언어와 목표 언어가 동일합니다");
        }
    }

    public String getSourceText() {
        return sourceText;
    }

    public Language getSourceLanguage() {
        return sourceLanguage;
    }

    public Language getTargetLanguage() {
        return targetLanguage;
    }

    public boolean isAutoDetect() {
        return sourceLanguage == null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranslationRequest that = (TranslationRequest) o;
        return Objects.equals(sourceText, that.sourceText) &&
                Objects.equals(sourceLanguage, that.sourceLanguage) &&
                Objects.equals(targetLanguage, that.targetLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceText, sourceLanguage, targetLanguage);
    }

    @Override
    public String toString() {
        return "TranslationRequest{" +
                "sourceLanguage=" + (sourceLanguage != null ? sourceLanguage : "AUTO") +
                ", targetLanguage=" + targetLanguage +
                ", textLength=" + sourceText.length() +
                '}';
    }
}
