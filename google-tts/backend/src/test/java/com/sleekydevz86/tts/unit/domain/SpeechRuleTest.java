package com.sleekydevz86.tts.unit.domain;

import com.sleekydevz86.tts.global.exception.TtsException;
import com.sleekydevz86.tts.speech.domain.model.Pitch;
import com.sleekydevz86.tts.speech.domain.model.SpeakingRate;
import com.sleekydevz86.tts.speech.domain.model.SpeechLanguage;
import com.sleekydevz86.tts.speech.domain.model.SpeechText;
import com.sleekydevz86.tts.speech.domain.model.VoiceGender;
import com.sleekydevz86.tts.speech.domain.model.VoiceStyle;
import com.sleekydevz86.tts.speech.domain.service.SpeechLanguageResolver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("음성 도메인 규칙")
class SpeechRuleTest {

    @Test
    @DisplayName("공백 텍스트는 거절한다")
    void rejectsBlankText() {
        // given
        String text = "   ";

        // when
        TtsException exception = assertThrows(TtsException.class, () -> new SpeechText(text));

        // then
        assertEquals("INVALID_TEXT", exception.code());
    }

    @Test
    @DisplayName("속도 범위를 벗어나면 거절한다")
    void rejectsSpeakingRateOutsideRange() {
        // given
        double rate = 4.1;

        // when
        TtsException exception = assertThrows(TtsException.class, () -> new SpeakingRate(rate));

        // then
        assertEquals("INVALID_RATE", exception.code());
    }

    @Test
    @DisplayName("음높이 범위를 벗어나면 거절한다")
    void rejectsPitchOutsideRange() {
        // given
        double pitch = 21.0;

        // when
        TtsException exception = assertThrows(TtsException.class, () -> new Pitch(pitch));

        // then
        assertEquals("INVALID_PITCH", exception.code());
    }

    @Test
    @DisplayName("네 언어의 남성 표준 목소리 이름을 고른다")
    void mapsMaleStandardVoices() {
        // given
        VoiceGender gender = VoiceGender.MALE;
        VoiceStyle style = VoiceStyle.STANDARD;

        // when
        String korean = SpeechLanguage.KO.voiceName(gender, style);
        String japanese = SpeechLanguage.JA.voiceName(gender, style);
        String chinese = SpeechLanguage.ZH.voiceName(gender, style);
        String english = SpeechLanguage.EN.voiceName(gender, style);

        // then
        assertEquals("ko-KR-Standard-C", korean);
        assertEquals("ja-JP-Standard-C", japanese);
        assertEquals("cmn-CN-Standard-B", chinese);
        assertEquals("en-US-Standard-B", english);
    }

    @Test
    @DisplayName("알 수 없는 언어 코드는 거절한다")
    void rejectsUnknownLanguage() {
        // given
        SpeechLanguageResolver resolver = new SpeechLanguageResolver();

        // when
        TtsException exception = assertThrows(TtsException.class, () -> resolver.resolve("fr-FR"));

        // then
        assertEquals("INVALID_LANGUAGE", exception.code());
    }
}
