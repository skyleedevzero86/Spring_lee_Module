package com.sleekydz86.monitoring.logstack_s3.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.sleekydz86.monitoring.logstack_s3.support.TestFileFixtures;

@DisplayName("FileIdGenerator 단위 테스트")
class FileIdGeneratorTest {

    @Test
    @DisplayName("성공 - 날짜시각 prefix 생성")
    void dateTimePrefix_success() {
        // given
        String ownerPrefix = "lky";

        // when
        String prefix = FileIdGenerator.dateTimePrefix(ownerPrefix, TestFileFixtures.FIXED_TIME);

        // then
        assertThat(prefix).isEqualTo("lky_20260520_1430");
    }

    @Test
    @DisplayName("성공 - 첫 번째 ID는 4자리 순번")
    void nextId_firstSequence_success() {
        // given
        String dateTimePrefix = "lky_20260520_1430";
        long currentMax = 0;

        // when
        String id = FileIdGenerator.nextId(dateTimePrefix, currentMax);

        // then
        assertThat(id).isEqualTo("lky_20260520_1430_0001");
    }

    @Test
    @DisplayName("성공 - 9999 다음은 10000")
    void formatId_after9999_success() {
        // given
        String dateTimePrefix = "lky_20260520_1430";
        long sequence = 10_000L;

        // when
        String id = FileIdGenerator.formatId(dateTimePrefix, sequence);

        // then
        assertThat(id).isEqualTo("lky_20260520_1430_10000");
    }

    @Test
    @DisplayName("성공 - max 9999일 때 next는 10000")
    void nextId_from9999_success() {
        // given
        String dateTimePrefix = "lky_20260520_1430";
        long currentMax = 9999;

        // when
        String id = FileIdGenerator.nextId(dateTimePrefix, currentMax);

        // then
        assertThat(id).isEqualTo("lky_20260520_1430_10000");
    }
}
