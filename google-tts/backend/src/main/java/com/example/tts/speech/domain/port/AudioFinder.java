package com.example.tts.speech.domain.port;

import com.example.tts.speech.domain.model.AudioFileName;
import com.example.tts.speech.domain.model.AudioObject;
import com.example.tts.speech.domain.model.AudioSummary;

import java.util.List;
import java.util.Optional;

public interface AudioFinder {

    Optional<AudioObject> findOne(AudioFileName name);

    List<AudioSummary> findMany();

    boolean exists(AudioFileName name);
}
