package com.sleekydz86.storagemigration.file.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("파일 정보")
class StoredFileTest {

    private static final Instant NOW = Instant.parse("2026-11-11T00:00:00Z");

    @Test
    @DisplayName("상세 정보를 고치면 공백을 지우고 태그 중복을 없앤다")
    void normalizesTagsOnDetailUpdate() {
        // given
        StoredFile file = StoredFile.uploaded("1", "s", "b", "k", "a.pdf", null, 10, null, NOW);

        // when
        file.updateDetail(" 제목 ", " ", List.of(" irb ", "", "irb", "보고서"), NOW);

        // then
        assertThat(file.title()).isEqualTo("제목");
        assertThat(file.description()).isNull();
        assertThat(file.tagsText()).isEqualTo("irb,보고서");
    }

    @Test
    @DisplayName("DB 저장을 표시하면 체크섬과 저장 시각이 남는다")
    void markSavedRecordsChecksumAndTime() {
        // given
        StoredFile file = StoredFile.uploaded("1", "s", "b", "k", "a.pdf", null, 10, null, NOW);

        // when
        file.markSaved("abc", NOW.plusSeconds(5));

        // then
        assertThat(file.isSaved()).isTrue();
        assertThat(file.checksum()).isEqualTo("abc");
        assertThat(file.savedAt()).isEqualTo(NOW.plusSeconds(5));
    }
}
