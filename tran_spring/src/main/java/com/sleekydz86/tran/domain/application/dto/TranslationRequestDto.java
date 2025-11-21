package com.sleekydz86.tran.domain.application.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class TranslationRequestDto {

    @NotBlank(message = "번역할 텍스트를 입력해주세요")
    @Size(max = 5000, message = "번역할 텍스트는 5000자를 초과할 수 없습니다")
    private String text;

    private String sourceLanguage; // null이면 자동 감지

    @NotBlank(message = "목표 언어를 선택해주세요")
    private String targetLanguage;

    public TranslationRequestDto() {
    }

    public TranslationRequestDto(String text, String sourceLanguage, String targetLanguage) {
        this.text = text;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public boolean isAutoDetect() {
        return sourceLanguage == null || sourceLanguage.isBlank() || "auto".equalsIgnoreCase(sourceLanguage);
    }

    @Override
    public String toString() {
        return "TranslationRequestDto{" +
                "sourceLanguage='" + (sourceLanguage != null ? sourceLanguage : "AUTO") + '\'' +
                ", targetLanguage='" + targetLanguage + '\'' +
                ", textLength=" + (text != null ? text.length() : 0) +
                '}';
    }
}