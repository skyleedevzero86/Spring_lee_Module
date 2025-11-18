package com.sleekydz86.payment2v2.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DateTimeUtil 테스트")
class DateTimeUtilTest {

    @Test
    @DisplayName("LocalDateTime을 기본 형식 문자열로 변환할 수 있다")
    void LocalDateTime을_기본_형식_문자열로_변환할_수_있다() {

        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 14, 30, 45);

        String formatted = DateTimeUtil.format(dateTime);

        assertThat(formatted).isEqualTo("2024-01-15 14:30:45");
    }

    @Test
    @DisplayName("null LocalDateTime을 변환하면 null을 반환한다")
    void null_LocalDateTime을_변환하면_null을_반환한다() {

        LocalDateTime dateTime = null;

        String formatted = DateTimeUtil.format(dateTime);

        assertThat(formatted).isNull();
    }

    @Test
    @DisplayName("기본 형식 문자열을 LocalDateTime으로 파싱할 수 있다")
    void 기본_형식_문자열을_LocalDateTime으로_파싱할_수_있다() {

        String dateTimeString = "2024-01-15 14:30:45";

        LocalDateTime parsed = DateTimeUtil.parse(dateTimeString);

        assertThat(parsed).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30, 45));
    }

    @Test
    @DisplayName("null 문자열을 파싱하면 null을 반환한다")
    void null_문자열을_파싱하면_null을_반환한다() {

        String dateTimeString = null;

        LocalDateTime parsed = DateTimeUtil.parse(dateTimeString);

        assertThat(parsed).isNull();
    }

    @Test
    @DisplayName("빈 문자열을 파싱하면 null을 반환한다")
    void 빈_문자열을_파싱하면_null을_반환한다() {

        String dateTimeString = "";

        LocalDateTime parsed = DateTimeUtil.parse(dateTimeString);

        assertThat(parsed).isNull();
    }

    @Test
    @DisplayName("포맷과 파싱이 서로 역변환이 가능하다")
    void 포맷과_파싱이_서로_역변환이_가능하다() {

        LocalDateTime original = LocalDateTime.of(2024, 1, 15, 14, 30, 45);

        String formatted = DateTimeUtil.format(original);
        LocalDateTime parsed = DateTimeUtil.parse(formatted);

        assertThat(parsed).isEqualTo(original);
    }

    @Test
    @DisplayName("다양한 날짜 시간을 포맷할 수 있다")
    void 다양한_날짜_시간을_포맷할_수_있다() {

        LocalDateTime[] dateTimes = {
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 12, 31, 23, 59, 59),
                LocalDateTime.of(2024, 6, 15, 12, 30, 0)
        };

        String[] expected = {
                "2024-01-01 00:00:00",
                "2024-12-31 23:59:59",
                "2024-06-15 12:30:00"
        };

        for (int i = 0; i < dateTimes.length; i++) {

            String formatted = DateTimeUtil.format(dateTimes[i]);

            assertThat(formatted).isEqualTo(expected[i]);
        }
    }
}
