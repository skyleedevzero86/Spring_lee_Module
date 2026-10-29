package com.sleekydevz86.tts.unit.util;

import com.sleekydevz86.tts.global.util.AudioFileNameNormalizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("음성 파일 이름 정규화")
class AudioFileNameNormalizerTest {

    private final AudioFileNameNormalizer normalizer = new AudioFileNameNormalizer();

    @Test
    @DisplayName("확장자가 없으면 mp3를 붙이고 한글 이름을 유지한다")
    void appendsMp3AndKeepsHangul() {
        // given
        String english = "hello";
        String hangul = "안녕";

        // when
        String englishName = normalizer.normalize(english);
        String hangulName = normalizer.normalize(hangul);
        String duplicated = normalizer.normalize("hello.mp3");

        // then
        assertEquals("hello.mp3", englishName);
        assertEquals("안녕.mp3", hangulName);
        assertEquals("hello.mp3", duplicated);
    }

    @Test
    @DisplayName("경로와 안전하지 않은 문자를 제거한다")
    void dropsDirectoriesAndUnsafeCharacters() {
        // given
        String traversal = "../etc/passwd";
        String spaced = "hello world!.mp3";

        // when
        String traversalName = normalizer.normalize(traversal);
        String spacedName = normalizer.normalize(spaced);

        // then
        assertEquals("passwd.mp3", traversalName);
        assertEquals("hello_world.mp3", spacedName);
    }
}
