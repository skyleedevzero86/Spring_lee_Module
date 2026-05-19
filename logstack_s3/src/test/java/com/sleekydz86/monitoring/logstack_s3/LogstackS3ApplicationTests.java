package com.sleekydz86.monitoring.logstack_s3;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("애플리케이션 테스트")
class LogstackS3ApplicationTests {

    @Test
    @DisplayName("성공 - 메인 클래스 로드")
    void mainClassLoads_success() {
        // given
        String mainClassName = "com.sleekydz86.monitoring.logstack_s3.LogstackS3Application";

        // when & then
        assertThatCode(() -> Class.forName(mainClassName))
                .doesNotThrowAnyException();
    }
}
