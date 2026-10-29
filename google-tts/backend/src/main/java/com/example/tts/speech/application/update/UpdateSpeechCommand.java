package com.example.tts.speech.application.update;

public record UpdateSpeechCommand(
        String text,
        String filename,
        String language,
        String gender,
        String voiceStyle,
        Double speakingRate,
        Double pitch
) {
}
