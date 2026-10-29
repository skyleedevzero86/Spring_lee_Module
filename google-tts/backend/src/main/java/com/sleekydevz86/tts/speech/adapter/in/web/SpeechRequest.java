package com.sleekydevz86.tts.speech.adapter.in.web;

public record SpeechRequest(
        String text,
        String filename,
        String language,
        String gender,
        String voiceStyle,
        Double speakingRate,
        Double pitch
) {
}
