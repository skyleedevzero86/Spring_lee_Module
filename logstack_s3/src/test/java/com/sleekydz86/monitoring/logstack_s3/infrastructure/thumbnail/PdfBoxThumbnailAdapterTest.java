package com.sleekydz86.monitoring.logstack_s3.infrastructure.thumbnail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@DisplayName("PdfBoxThumbnailAdapter 테스트")
class PdfBoxThumbnailAdapterTest {

    private final PdfBoxThumbnailAdapter adapter = new PdfBoxThumbnailAdapter();

    @Test
    @DisplayName("성공 - placeholder 생성")
    void placeholder_success() {
        // given

        // when
        byte[] bytes = adapter.placeholder();

        // then
        assertThat(bytes).isNotEmpty();
    }

    @Test
    @DisplayName("성공 - image/pdf 지원 여부")
    void supports_success() {
        // given when & then
        assertThat(adapter.supports("image/png")).isTrue();
        assertThat(adapter.supports("application/pdf")).isTrue();
        assertThat(adapter.supports("text/plain")).isFalse();
    }

    @Test
    @DisplayName("성공 - 텍스트 파일은 썸네일 생성 skip")
    void generate_text_empty_success() {
        // given
        var file = TestFileFixtures.textMultipartFile();

        // when
        Optional<byte[]> result = adapter.generate(file, "text/plain");

        // then
        assertThat(result).isEmpty();
    }
}
