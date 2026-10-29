package com.example.tts.speech.domain.port;

import com.example.tts.speech.domain.model.Speech;

public interface SpeechSynthesizer {

    byte[] synthesize(Speech speech);
}
