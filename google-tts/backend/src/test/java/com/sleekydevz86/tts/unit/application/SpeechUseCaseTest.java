package com.sleekydevz86.tts.unit.application;

import com.sleekydevz86.tts.global.util.AudioFileNameNormalizer;
import com.sleekydevz86.tts.speech.application.create.CreateSpeechCommand;
import com.sleekydevz86.tts.speech.application.create.CreateSpeechService;
import com.sleekydevz86.tts.speech.application.delete.DeleteSpeechService;
import com.sleekydevz86.tts.speech.application.factory.SpeechFactory;
import com.sleekydevz86.tts.speech.application.model.AudioContent;
import com.sleekydevz86.tts.speech.application.multi.FindSpeechesService;
import com.sleekydevz86.tts.speech.application.policy.StoredAudioNamePolicy;
import com.sleekydevz86.tts.speech.application.single.FindSpeechService;
import com.sleekydevz86.tts.speech.application.update.UpdateSpeechCommand;
import com.sleekydevz86.tts.speech.application.update.UpdateSpeechService;
import com.sleekydevz86.tts.global.exception.TtsException;
import com.sleekydevz86.tts.speech.domain.service.SpeechLanguageResolver;
import com.sleekydevz86.tts.support.FixedSpeechSynthesizer;
import com.sleekydevz86.tts.support.InMemoryAudioStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("음성 유스케이스")
class SpeechUseCaseTest {

    @Test
    @DisplayName("생성은 기본 여성 Neural2 목소리로 저장한다")
    void createsWithFemaleNeuralDefaults() {
        // given
        InMemoryAudioStore store = new InMemoryAudioStore();
        FixedSpeechSynthesizer synthesizer = new FixedSpeechSynthesizer();
        CreateSpeechService create = createService(store, synthesizer);
        CreateSpeechCommand command = new CreateSpeechCommand("  안녕하세요  ", "hello", "ko-KR", null, null, null, null);

        // when
        AudioContent audio = create.execute(command);

        // then
        assertEquals("hello.mp3", audio.fileName());
        assertArrayEquals("안녕하세요".getBytes(StandardCharsets.UTF_8), audio.content());
        assertEquals("ko-KR-Neural2-A", synthesizer.requests().getFirst().language().voiceName(
                synthesizer.requests().getFirst().gender(),
                synthesizer.requests().getFirst().voiceStyle()
        ));
        assertTrue(store.exists(synthesizer.requests().getFirst().fileName()));
    }

    @Test
    @DisplayName("없는 파일은 수정하지 않고 합성을 호출하지 않는다")
    void doesNotSynthesizeMissingUpdate() {
        // given
        InMemoryAudioStore store = new InMemoryAudioStore();
        FixedSpeechSynthesizer synthesizer = new FixedSpeechSynthesizer();
        UpdateSpeechService update = updateService(store, synthesizer);
        UpdateSpeechCommand command = new UpdateSpeechCommand("hello", "missing.mp3", "en-US", "MALE", "STANDARD", 1.0, 0.0);

        // when
        TtsException exception = assertThrows(TtsException.class, () -> update.execute(command));

        // then
        assertEquals("NOT_FOUND", exception.code());
        assertTrue(synthesizer.requests().isEmpty());
    }

    @Test
    @DisplayName("없는 파일 삭제는 거절한다")
    void rejectsDeletingMissingFile() {
        // given
        InMemoryAudioStore store = new InMemoryAudioStore();
        DeleteSpeechService delete = deleteService(store);

        // when
        TtsException exception = assertThrows(TtsException.class, () -> delete.execute("missing.mp3"));

        // then
        assertEquals("NOT_FOUND", exception.code());
    }

    @Test
    @DisplayName("단건 조회는 저장한 바이트를 반환한다")
    void findsStoredBytes() {
        // given
        InMemoryAudioStore store = new InMemoryAudioStore();
        FixedSpeechSynthesizer synthesizer = new FixedSpeechSynthesizer();
        createService(store, synthesizer).execute(new CreateSpeechCommand("hello", "hello.mp3", "en-US", "FEMALE", "STANDARD", 1.0, -2.0));
        FindSpeechService find = new FindSpeechService(names(), store);

        // when
        AudioContent audio = find.execute("hello.mp3");

        // then
        assertArrayEquals("hello".getBytes(StandardCharsets.UTF_8), audio.content());
    }

    @Test
    @DisplayName("복수건 조회는 저장한 파일을 포함한다")
    void findsManyStoredFiles() {
        // given
        InMemoryAudioStore store = new InMemoryAudioStore();
        FixedSpeechSynthesizer synthesizer = new FixedSpeechSynthesizer();
        createService(store, synthesizer).execute(new CreateSpeechCommand("hello", "hello.mp3", "ja-JP", "MALE", "NEURAL2", 1.2, 1.0));
        FindSpeechesService findMany = new FindSpeechesService(store);

        // when
        boolean found = findMany.execute().stream().anyMatch(summary -> "hello.mp3".equals(summary.fileName()));

        // then
        assertTrue(found);
    }

    private CreateSpeechService createService(InMemoryAudioStore store, FixedSpeechSynthesizer synthesizer) {
        return new CreateSpeechService(factory(), synthesizer, store);
    }

    private UpdateSpeechService updateService(InMemoryAudioStore store, FixedSpeechSynthesizer synthesizer) {
        return new UpdateSpeechService(factory(), names(), store, synthesizer, store);
    }

    private DeleteSpeechService deleteService(InMemoryAudioStore store) {
        return new DeleteSpeechService(names(), store, store);
    }

    private SpeechFactory factory() {
        return new SpeechFactory(new AudioFileNameNormalizer(), new SpeechLanguageResolver());
    }

    private StoredAudioNamePolicy names() {
        return new StoredAudioNamePolicy(new AudioFileNameNormalizer());
    }
}
