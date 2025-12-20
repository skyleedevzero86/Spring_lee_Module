package com.sleekydz86.passykey.domain.service;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.global.exception.InvalidCounterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CredentialDomainService 테스트")
class CredentialDomainServiceTest {

    private CredentialDomainService credentialDomainService;
    private User user;
    private WebAuthnCredential credential;

    @BeforeEach
    void setUp() {
        credentialDomainService = new CredentialDomainService();
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
    @DisplayName("카운터 검증 및 업데이트 성공")
    void validateAndUpdateCounter_Success() {
        // given
        Long initialCounter = credential.getCounter();
        Long newCounter = 10L;

        // when
        credentialDomainService.validateAndUpdateCounter(credential, newCounter);

        // then
        assertThat(credential.getCounter()).isEqualTo(newCounter);
        assertThat(credential.getCounter()).isGreaterThan(initialCounter);
    }

    @Test
    @DisplayName("카운터 검증 실패 - 새 카운터가 현재 카운터보다 작음")
    void validateAndUpdateCounter_Fail_CounterTooSmall() {
        // given
        credential.setCounter(10L);
        Long newCounter = 5L;

        // when & then
        assertThatThrownBy(() -> credentialDomainService.validateAndUpdateCounter(credential, newCounter))
                .isInstanceOf(InvalidCounterException.class)
                .hasMessageContaining("카운터는 현재 값보다 커야 합니다");

        assertThat(credential.getCounter()).isEqualTo(10L);
    }

    @Test
    @DisplayName("카운터 검증 실패 - 새 카운터가 현재 카운터와 같음")
    void validateAndUpdateCounter_Fail_CounterEqual() {
        // given
        credential.setCounter(10L);
        Long newCounter = 10L;

        // when & then
        assertThatThrownBy(() -> credentialDomainService.validateAndUpdateCounter(credential, newCounter))
                .isInstanceOf(InvalidCounterException.class)
                .hasMessageContaining("카운터는 현재 값보다 커야 합니다");

        assertThat(credential.getCounter()).isEqualTo(10L);
    }
}


