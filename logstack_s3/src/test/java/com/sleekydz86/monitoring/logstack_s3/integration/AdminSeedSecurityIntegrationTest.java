package com.sleekydz86.monitoring.logstack_s3.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.sleekydz86.monitoring.logstack_s3.domain.message.DomainMessages;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestBase;
import com.sleekydz86.monitoring.logstack_s3.support.IntegrationTestFixtures;

@Tag("integration")
@DisplayName("시드 API 보안 통합 테스트")
class AdminSeedSecurityIntegrationTest extends IntegrationTestBase {

    @Test
    @DisplayName("실패 - API 키 없으면 401")
    void seed_withoutApiKey_unauthorized() {
        // when
        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/admin/seed?count=1",
                null,
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains(DomainMessages.ADMIN_API_KEY_REQUIRED);
    }

    @Test
    @DisplayName("실패 - 잘못된 API 키면 401")
    void seed_wrongApiKey_unauthorized() {
        // given
        var headers = new org.springframework.http.HttpHeaders();
        headers.set(
                com.sleekydz86.monitoring.logstack_s3.global.config.AdminApiKeyFilter.ADMIN_API_KEY_HEADER,
                "wrong-key");

        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/admin/seed?count=1",
                HttpMethod.POST,
                new HttpEntity<>(headers),
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains(DomainMessages.ADMIN_API_KEY_INVALID);
    }

    @Test
    @DisplayName("성공 - 올바른 API 키면 시드 허용")
    void seed_validApiKey_success() {
        // when
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl() + "/api/admin/seed?count=2",
                HttpMethod.POST,
                new HttpEntity<>(IntegrationTestFixtures.adminHeaders()),
                String.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("seeded");
    }
}
