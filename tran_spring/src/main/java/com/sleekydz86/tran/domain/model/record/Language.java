package com.sleekydz86.tran.domain.model.record;

import java.util.Objects;

public final class Language {
    private final String code;
    private final String displayName;

    public static final Language KOREAN = new Language("ko", "한국어");
    public static final Language ENGLISH = new Language("en", "영어");
    public static final Language JAPANESE = new Language("ja", "일본어");
    public static final Language CHINESE = new Language("zh", "중국어");
    public static final Language SPANISH = new Language("es", "스페인어");
    public static final Language FRENCH = new Language("fr", "프랑스어");
    public static final Language GERMAN = new Language("de", "독일어");

    private Language(String code, String displayName) {
        validateCode(code);
        this.code = code.toLowerCase();
        this.displayName = displayName;
    }

    public static Language of(String code) {
        return new Language(code, getDisplayNameByCode(code));
    }

    private static void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("언어 코드는 필수입니다");
        }
        if (code.length() < 2 || code.length() > 5) {
            throw new IllegalArgumentException("올바르지 않은 언어 코드 형식입니다");
        }
    }

    private static String getDisplayNameByCode(String code) {
        return switch (code.toLowerCase()) {
            case "ko" -> "한국어";
            case "en" -> "영어";
            case "ja" -> "일본어";
            case "zh" -> "중국어";
            case "es" -> "스페인어";
            case "fr" -> "프랑스어";
            case "de" -> "독일어";
            default -> code.toUpperCase();
        };
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isKorean() {
        return "ko".equals(code);
    }

    public boolean isEnglish() {
        return "en".equals(code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Language language = (Language) o;
        return Objects.equals(code, language.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return displayName + "(" + code + ")";
    }
}
