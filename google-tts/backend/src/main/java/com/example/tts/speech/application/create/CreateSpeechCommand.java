package com.example.tts.speech.application.create;

public record CreateSpeechCommand(
        String text,
        String filename,
        String language,
        String gender,
        String voiceStyle,
        Double speakingRate,
        Double pitch
) {
}
