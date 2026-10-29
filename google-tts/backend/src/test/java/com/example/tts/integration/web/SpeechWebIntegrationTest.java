package com.example.tts.integration.web;

import com.example.tts.global.config.SpeechDomainConfiguration;
import com.example.tts.global.exception.GlobalExceptionHandler;
import com.example.tts.global.util.AudioFileNameNormalizer;
import com.example.tts.speech.adapter.in.web.SpeechController;
import com.example.tts.speech.application.create.CreateSpeechService;
import com.example.tts.speech.application.delete.DeleteSpeechService;
import com.example.tts.speech.application.factory.SpeechFactory;
import com.example.tts.speech.application.multi.FindSpeechesService;
import com.example.tts.speech.application.policy.StoredAudioNamePolicy;
import com.example.tts.speech.application.single.FindSpeechService;
import com.example.tts.speech.application.update.UpdateSpeechService;
import com.example.tts.speech.domain.model.AudioFileName;
import com.example.tts.speech.domain.model.AudioObject;
import com.example.tts.speech.domain.model.AudioSummary;
import com.example.tts.speech.domain.port.AudioDeleter;
import com.example.tts.speech.domain.port.AudioFinder;
import com.example.tts.speech.domain.port.AudioSaver;
import com.example.tts.speech.domain.port.SpeechSynthesizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SpeechController.class)
@Import({
        CreateSpeechService.class,
        UpdateSpeechService.class,
        DeleteSpeechService.class,
        FindSpeechService.class,
        FindSpeechesService.class,
        SpeechFactory.class,
        StoredAudioNamePolicy.class,
        AudioFileNameNormalizer.class,
        SpeechDomainConfiguration.class,
        GlobalExceptionHandler.class
})
@DisplayName("음성 HTTP 통합")
class SpeechWebIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpeechSynthesizer synthesizer;

    @MockitoBean
    private AudioSaver saver;

    @MockitoBean
    private AudioFinder finder;

    @MockitoBean
    private AudioDeleter deleter;

    @Test
    @DisplayName("음성 생성 요청은 mp3 응답을 돌려준다")
    void createsSpeechAudio() throws Exception {
        // given
        given(synthesizer.synthesize(any())).willReturn("audio".getBytes(StandardCharsets.UTF_8));

        // when
        var result = mockMvc.perform(post("/api/tts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"text":"안녕하세요","filename":"hello","language":"ko-KR","gender":"FEMALE","voiceStyle":"NEURAL2","speakingRate":1.0,"pitch":0.0}
                        """));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().contentType("audio/mpeg"))
                .andExpect(header().string("X-Audio-Filename", "hello.mp3"));
        verify(saver).save(any());
    }

    @Test
    @DisplayName("빈 텍스트 생성은 규칙 오류를 돌려준다")
    void rejectsBlankText() throws Exception {
        // given
        String body = """
                {"text":"  ","filename":"hello","language":"ko-KR"}
                """;

        // when
        var result = mockMvc.perform(post("/api/tts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));

        // then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_TEXT"))
                .andExpect(jsonPath("$.message").value("텍스트를 입력해야 합니다."));
    }

    @Test
    @DisplayName("없는 음성 수정은 404를 돌려준다")
    void rejectsMissingUpdate() throws Exception {
        // given
        given(finder.exists(any())).willReturn(false);

        // when
        var result = mockMvc.perform(put("/api/tts/files/missing.mp3")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"text":"hello","language":"en-US","gender":"MALE","voiceStyle":"STANDARD","speakingRate":1.0,"pitch":0.0}
                        """));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("단건 조회는 저장소의 음성을 돌려준다")
    void findsOneAudio() throws Exception {
        // given
        given(finder.findOne(any())).willReturn(Optional.of(new AudioObject(
                new AudioFileName("hello.mp3"),
                "hello".getBytes(StandardCharsets.UTF_8)
        )));

        // when
        var result = mockMvc.perform(get("/api/tts/files/hello.mp3"));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().bytes("hello".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("복수건 조회는 저장소 목록을 돌려준다")
    void findsManyAudio() throws Exception {
        // given
        given(finder.findMany()).willReturn(List.of(new AudioSummary("hello.mp3", 5, Instant.parse("2026-10-29T00:00:00Z"))));

        // when
        var result = mockMvc.perform(get("/api/tts/files"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").value("hello.mp3"))
                .andExpect(jsonPath("$[0].size").value(5));
    }

    @Test
    @DisplayName("삭제는 저장소 삭제를 호출하고 본문 없이 끝난다")
    void deletesAudio() throws Exception {
        // given
        given(finder.exists(any())).willReturn(true);

        // when
        var result = mockMvc.perform(delete("/api/tts/files/hello.mp3"));

        // then
        result.andExpect(status().isNoContent());
        verify(deleter).delete(new AudioFileName("hello.mp3"));
    }
}
