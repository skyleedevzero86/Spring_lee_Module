package com.sleekydz86.monitoring.logstack_s3.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("FileKeyFactory 단위 테스트")
class FileKeyFactoryTest {

    @Test
    @DisplayName("성공 - 파일명 없으면 unknown")
    void defaultFilename_blank_success() {
        // given

        // when
        String fromNull = FileKeyFactory.defaultFilename(null);
        String fromBlank = FileKeyFactory.defaultFilename("  ");

        // then
        assertThat(fromNull).isEqualTo("unknown");
        assertThat(fromBlank).isEqualTo("unknown");
    }

    @Test
    @DisplayName("성공 - contentType 없으면 octet-stream")
    void defaultContentType_blank_success() {
        // given

        // when
        String contentType = FileKeyFactory.defaultContentType(null);

        // then
        assertThat(contentType).isEqualTo("application/octet-stream");
    }

    @Test
    @DisplayName("성공 - 위험 문자 파일명 정리")
    void sanitize_success() {
        // given
        String rawName = "a/b:c?.txt";

        // when
        String sanitized = FileKeyFactory.sanitize(rawName);

        // then
        assertThat(sanitized).isEqualTo("a_b_c_.txt");
    }

    @Test
    @DisplayName("성공 - uploadKey는 uploads/ 로 시작")
    void uploadKey_success() {
        // given
        String filename = "test.txt";

        // when
        String key = FileKeyFactory.uploadKey(filename);

        // then
        assertThat(key).startsWith("uploads/");
    }
}
