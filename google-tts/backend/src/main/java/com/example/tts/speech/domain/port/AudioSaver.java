package com.example.tts.speech.domain.port;

import com.example.tts.speech.domain.model.AudioObject;

public interface AudioSaver {

    void save(AudioObject audio);
}
