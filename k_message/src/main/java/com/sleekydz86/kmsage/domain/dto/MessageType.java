package com.sleekydz86.kmsage.domain.dto;

public enum MessageType {
    SMS("단문 문자"),
    LMS("장문 문자"),
    MMS("사진 문자"),
    KAKAO_ALIMTALK("카카오 알림톡"),
    KAKAO_FRIENDTALK("카카오 친구톡");

    private final String description;

    MessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}