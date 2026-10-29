package com.example.tts.speech.domain.port;

import com.example.tts.speech.domain.model.AudioFileName;

public interface AudioDeleter {

    void delete(AudioFileName name);
}
