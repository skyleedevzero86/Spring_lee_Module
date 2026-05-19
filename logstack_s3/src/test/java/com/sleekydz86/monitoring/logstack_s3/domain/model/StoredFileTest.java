package com.sleekydz86.monitoring.logstack_s3.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@DisplayName("StoredFile 도메인 모델 테스트")
class StoredFileTest {

    @Test
    @DisplayName("성공 - image/pdf 판별")
    void mediaTypeFlags_success() {
        // given
        StoredFile image = TestFileFixtures.storedFile();

        // when
        // then
        assertThat(image.isImage()).isTrue();
        assertThat(image.isPdf()).isFalse();
        assertThat(image.thumbnailKeyOptional()).isPresent();
    }

    @Test
    @DisplayName("성공 - 썸네일 키 없으면 empty")
    void thumbnailKeyOptional_blank_success() {
        // given
        StoredFile file = new StoredFile(
                "id", "a", "k", "  ", "text/plain", 1L, TestFileFixtures.FIXED_TIME
        );

        // when
        var thumbnailKey = file.thumbnailKeyOptional();

        // then
        assertThat(thumbnailKey).isEmpty();
    }
}
