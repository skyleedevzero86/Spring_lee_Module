package com.sleekydz86.monitoring.logstack_s3.infrastructure.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SiteFaviconRenderer 테스트")
class SiteFaviconRendererTest {

    private final SiteFaviconRenderer renderer = new SiteFaviconRenderer();

    @Test
    @DisplayName("성공 - PNG 파비콘 바이트 생성")
    void renderPng_success() {
        byte[] png = renderer.renderPng();

        assertThat(png).isNotEmpty();
        assertThat(png[0]).isEqualTo((byte) 0x89);
        assertThat(png[1]).isEqualTo((byte) 'P');
        assertThat(png[2]).isEqualTo((byte) 'N');
        assertThat(png[3]).isEqualTo((byte) 'G');
    }
}
