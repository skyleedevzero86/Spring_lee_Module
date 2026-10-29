package com.example.tts.speech.domain.model;

public record Speech(
        SpeechText text,
        AudioFileName fileName,
        SpeechLanguage language,
        VoiceGender gender,
        VoiceStyle voiceStyle,
        SpeakingRate speakingRate,
        Pitch pitch
) {
}
