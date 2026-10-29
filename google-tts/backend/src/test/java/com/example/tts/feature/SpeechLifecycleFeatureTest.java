package com.example.tts.feature;

import com.example.tts.global.util.AudioFileNameNormalizer;
import com.example.tts.speech.application.create.CreateSpeechCommand;
import com.example.tts.speech.application.create.CreateSpeechService;
import com.example.tts.speech.application.delete.DeleteSpeechService;
import com.example.tts.speech.application.factory.SpeechFactory;
import com.example.tts.speech.application.model.AudioContent;
import com.example.tts.speech.application.multi.FindSpeechesService;
import com.example.tts.speech.application.policy.StoredAudioNamePolicy;
import com.example.tts.speech.application.single.FindSpeechService;
import com.example.tts.speech.application.update.UpdateSpeechCommand;
import com.example.tts.speech.application.update.UpdateSpeechService;
import com.example.tts.speech.domain.exception.SpeechRuleException;
import com.example.tts.speech.domain.service.SpeechLanguageResolver;
import com.example.tts.support.FixedSpeechSynthesizer;
import com.example.tts.support.InMemoryAudioStore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("음성 생명주기")
class SpeechLifecycleFeatureTest {

    @Test
    @DisplayName("음성을 만들고 조회한 뒤 수정하고 삭제한다")
    void managesSpeechLifecycle() {
        // given
        InMemoryAudioStore store = new InMemoryAudioStore();
        FixedSpeechSynthesizer synthesizer = new FixedSpeechSynthesizer();
        SpeechFactory factory = new SpeechFactory(new AudioFileNameNormalizer(), new SpeechLanguageResolver());
        StoredAudioNamePolicy names = new StoredAudioNamePolicy(new AudioFileNameNormalizer());
        CreateSpeechService create = new CreateSpeechService(factory, synthesizer, store);
        UpdateSpeechService update = new UpdateSpeechService(factory, names, store, synthesizer, store);
        DeleteSpeechService delete = new DeleteSpeechService(names, store, store);
        FindSpeechService findOne = new FindSpeechService(names, store);
        FindSpeechesService findMany = new FindSpeechesService(store);

        // when
        AudioContent created = create.execute(new CreateSpeechCommand(
                "안녕하세요", "lifecycle", "ko-KR", "FEMALE", "NEURAL2", 1.0, 0.0
        ));
        AudioContent found = findOne.execute(created.fileName());
        AudioContent updated = update.execute(new UpdateSpeechCommand(
                "こんにちは", created.fileName(), "ja-JP", "MALE", "STANDARD", 0.8, -1.0
        ));
        long countAfterUpdate = findMany.execute().stream()
                .filter(summary -> created.fileName().equals(summary.fileName()))
                .count();
        delete.execute(created.fileName());

        // then
        assertEquals("lifecycle.mp3", created.fileName());
        assertArrayEquals("안녕하세요".getBytes(StandardCharsets.UTF_8), found.content());
        assertArrayEquals("こんにちは".getBytes(StandardCharsets.UTF_8), updated.content());
        assertEquals(1, countAfterUpdate);
        assertTrue(findMany.execute().isEmpty());
        assertEquals("NOT_FOUND", assertThrows(SpeechRuleException.class, () -> findOne.execute(created.fileName())).code());
    }
}
