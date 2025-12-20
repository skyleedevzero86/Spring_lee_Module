package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnCredentialRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CredentialManagementUseCaseImpl 테스트")
class CredentialManagementUseCaseImplTest {

    @Mock
    private WebAuthnCredentialRepositoryPort credentialRepository;

    @InjectMocks
    private CredentialManagementUseCaseImpl credentialManagementUseCase;

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
    @DisplayName("사용자 인증서 목록 조회 성공")
    void getUserCredentials_Success() {
        // given
        List<WebAuthnCredential> credentials = Arrays.asList(credential);
        when(credentialRepository.findByUser(user)).thenReturn(credentials);

        // when
        List<WebAuthnCredential> result = credentialManagementUseCase.getUserCredentials(user);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCredentialId()).isEqualTo("credentialId");
        verify(credentialRepository, times(1)).findByUser(user);
    }

    @Test
    @DisplayName("인증서 삭제 성공")
    void deleteCredential_Success() {
        // given
        doNothing().when(credentialRepository).deleteByCredentialId(anyString());

        // when
        credentialManagementUseCase.deleteCredential("credentialId");

        // then
        verify(credentialRepository, times(1)).deleteByCredentialId("credentialId");
    }

    @Test
    @DisplayName("인증서 삭제 실패")
    void deleteCredential_Fail() {
        // given
        doThrow(new RuntimeException("삭제 실패"))
                .when(credentialRepository).deleteByCredentialId(anyString());

        // when & then
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> credentialManagementUseCase.deleteCredential("credentialId")
        );

        verify(credentialRepository, times(1)).deleteByCredentialId("credentialId");
    }
}

