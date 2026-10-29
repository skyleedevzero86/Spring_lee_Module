package com.example.tts.speech.domain.model;

public enum SpeechLanguage {
    KO("ko-KR"),
    JA("ja-JP"),
    ZH("cmn-CN"),
    EN("en-US");

    private final String code;

    SpeechLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public String voiceName(VoiceGender gender, VoiceStyle style) {
        boolean male = gender == VoiceGender.MALE;
        return switch (this) {
            case KO -> male
                    ? style == VoiceStyle.NEURAL2 ? "ko-KR-Neural2-C" : "ko-KR-Standard-C"
                    : style == VoiceStyle.NEURAL2 ? "ko-KR-Neural2-A" : "ko-KR-Standard-A";
            case JA -> male
                    ? style == VoiceStyle.NEURAL2 ? "ja-JP-Neural2-C" : "ja-JP-Standard-C"
                    : style == VoiceStyle.NEURAL2 ? "ja-JP-Neural2-B" : "ja-JP-Standard-A";
            case ZH -> male
                    ? style == VoiceStyle.NEURAL2 ? "cmn-CN-Neural2-B" : "cmn-CN-Standard-B"
                    : style == VoiceStyle.NEURAL2 ? "cmn-CN-Neural2-A" : "cmn-CN-Standard-A";
            case EN -> male
                    ? style == VoiceStyle.NEURAL2 ? "en-US-Neural2-D" : "en-US-Standard-B"
                    : style == VoiceStyle.NEURAL2 ? "en-US-Neural2-C" : "en-US-Standard-C";
        };
    }
}
