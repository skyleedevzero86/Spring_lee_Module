package com.example.tts.support;

import com.example.tts.speech.domain.model.AudioFileName;
import com.example.tts.speech.domain.model.AudioObject;
import com.example.tts.speech.domain.model.AudioSummary;
import com.example.tts.speech.domain.port.AudioDeleter;
import com.example.tts.speech.domain.port.AudioFinder;
import com.example.tts.speech.domain.port.AudioSaver;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryAudioStore implements AudioSaver, AudioFinder, AudioDeleter {

    private final Map<String, AudioObject> objects = new ConcurrentHashMap<>();
    private final Map<String, Instant> modifiedAt = new ConcurrentHashMap<>();

    @Override
    public void save(AudioObject audio) {
        objects.put(audio.name().value(), audio);
        modifiedAt.put(audio.name().value(), Instant.parse("2026-10-29T00:00:00Z").plusSeconds(objects.size()));
    }

    @Override
    public Optional<AudioObject> findOne(AudioFileName name) {
        return Optional.ofNullable(objects.get(name.value()));
    }

    @Override
    public List<AudioSummary> findMany() {
        return objects.values().stream()
                .map(audio -> new AudioSummary(
                        audio.name().value(),
                        audio.content().length,
                        modifiedAt.getOrDefault(audio.name().value(), Instant.EPOCH)
                ))
                .sorted(Comparator.comparing(AudioSummary::modifiedAt).reversed())
                .toList();
    }

    @Override
    public boolean exists(AudioFileName name) {
        return objects.containsKey(name.value());
    }

    @Override
    public void delete(AudioFileName name) {
        objects.remove(name.value());
        modifiedAt.remove(name.value());
    }
}
