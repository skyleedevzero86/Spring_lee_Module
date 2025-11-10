package com.sleekydz86.passykey.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WebAuthnCredential 도메인 모델 테스트")
class WebAuthnCredentialTest {

    private User user;
    private WebAuthnCredential credential;

    @BeforeEach
    void setUp() {
        user = new User(
                "testuser",
                "encodedPassword",
                "test@example.com",
                "Test User",
                "userHandle123"
        );
        user.setId(1L);

        credential = new WebAuthnCredential(
                "credentialId",
                "publicKeyCose",
                0L,
                "usb,nfc",
                user
        );
        credential.setId(1L);
    }

    @Test
    @DisplayName("WebAuthnCredential 생성 및 기본 정보 확인")
    void createCredential_Success() {
        // then
        assertThat(credential.getId()).isEqualTo(1L);
        assertThat(credential.getCredentialId()).isEqualTo("credentialId");
        assertThat(credential.getPublicKeyCose()).isEqualTo("publicKeyCose");
        assertThat(credential.getCounter()).isEqualTo(0L);
        assertThat(credential.getTransports()).isEqualTo("usb,nfc");
        assertThat(credential.getUser()).isEqualTo(user);
        assertThat(credential.getCreatedAt()).isNotNull();
        assertThat(credential.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("WebAuthnCredential 필드 수정")
    void updateCredentialFields_Success() {
        // when
        credential.setCredentialId("newCredentialId");
        credential.setPublicKeyCose("newPublicKeyCose");
        credential.setTransports("usb");
        credential.setLabel("My Passkey");

        // then
        assertThat(credential.getCredentialId()).isEqualTo("newCredentialId");
        assertThat(credential.getPublicKeyCose()).isEqualTo("newPublicKeyCose");
        assertThat(credential.getTransports()).isEqualTo("usb");
        assertThat(credential.getLabel()).isEqualTo("My Passkey");
    }

    @Test
    @DisplayName("마지막 사용 시간 업데이트")
    void updateLastUsed_Success() throws InterruptedException {
        // given
        LocalDateTime initialLastUsed = credential.getLastUsedAt();
        
        // when
        Thread.sleep(10); // 시간 차이를 만들기 위해
        credential.updateLastUsed();

        // then
        assertThat(credential.getLastUsedAt()).isAfter(initialLastUsed);
    }

    @Test
    @DisplayName("카운터 업데이트 성공")
    void updateCounter_Success() {
        // given
        Long initialCounter = credential.getCounter();
        Long newCounter = 10L;

        // when
        credential.updateCounter(newCounter);

        // then
        assertThat(credential.getCounter()).isEqualTo(newCounter);
        assertThat(credential.getCounter()).isGreaterThan(initialCounter);
        assertThat(credential.getLastUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("카운터 업데이트 실패 - 새 카운터가 현재 카운터보다 작음")
    void updateCounter_Fail_CounterTooSmall() {
        // given
        credential.setCounter(10L);
        Long newCounter = 5L;

        // when & then
        assertThatThrownBy(() -> credential.updateCounter(newCounter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("카운터는 현재 값보다 커야 합니다");

        assertThat(credential.getCounter()).isEqualTo(10L);
    }

    @Test
    @DisplayName("카운터 업데이트 실패 - 새 카운터가 현재 카운터와 같음")
    void updateCounter_Fail_CounterEqual() {
        // given
        credential.setCounter(10L);
        Long newCounter = 10L;

        // when & then
        assertThatThrownBy(() -> credential.updateCounter(newCounter))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("카운터는 현재 값보다 커야 합니다");

        assertThat(credential.getCounter()).isEqualTo(10L);
    }

    @Test
    @DisplayName("생성 시간 및 마지막 사용 시간 설정")
    void setTimestamps_Success() {
        // given
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime lastUsedAt = LocalDateTime.now();

        // when
        credential.setCreatedAt(createdAt);
        credential.setLastUsedAt(lastUsedAt);

        // then
        assertThat(credential.getCreatedAt()).isEqualTo(createdAt);
        assertThat(credential.getLastUsedAt()).isEqualTo(lastUsedAt);
    }
}

