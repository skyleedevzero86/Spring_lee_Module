package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.outbound.ChallengeServicePort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnConfigPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnCredentialRepositoryPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnOptionsFactoryPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnVerifierPort;
import com.sleekydz86.passykey.domain.service.CredentialDomainService;
import com.sleekydz86.passykey.global.constants.WebAuthnConstants;
import com.sleekydz86.passykey.global.exception.ChallengeExpiredException;
import com.sleekydz86.passykey.global.exception.CredentialNotFoundException;
import com.sleekydz86.passykey.global.exception.WebAuthnException;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.data.client.challenge.DefaultChallenge;
import com.webauthn4j.server.ServerProperty;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebAuthnAuthenticationUseCaseImpl 테스트")
class WebAuthnAuthenticationUseCaseImplTest {

    @Mock
    private WebAuthnCredentialRepositoryPort credentialRepository;

    @Mock
    private ChallengeServicePort challengeService;

    @Mock
    private WebAuthnOptionsFactoryPort optionsFactory;

    @Mock
    private WebAuthnVerifierPort verifierPort;

    @Mock
    private WebAuthnConfigPort configPort;

    @Mock
    private CredentialDomainService credentialDomainService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private WebAuthnAuthenticationUseCaseImpl authenticationUseCase;

    private User user;
    private WebAuthnCredential credential;
    private Challenge challenge;
    private PublicKeyCredentialRequestOptions options;

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

        challenge = new DefaultChallenge(new byte[]{1, 2, 3, 4});
        options = mock(PublicKeyCredentialRequestOptions.class);

        when(session.getId()).thenReturn("sessionId");
        when(configPort.getRpId()).thenReturn("example.com");
        when(configPort.getAllowedOrigins()).thenReturn("https://example.com");
    }

    @Test
    @DisplayName("인증 옵션 생성 성공")
    void createAuthenticationOptions_Success() {
        // given
        List<WebAuthnCredential> credentials = Arrays.asList(credential);
        when(challengeService.generateAndStoreChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION))
                .thenReturn(challenge);
        when(credentialRepository.findByUser(user)).thenReturn(credentials);
        when(optionsFactory.createAuthenticationOptions(
                eq(challenge), eq("example.com"), eq(credentials)))
                .thenReturn(options);

        // when
        PublicKeyCredentialRequestOptions result = authenticationUseCase.createAuthenticationOptions(user, session);

        // then
        assertThat(result).isNotNull();
        verify(challengeService, times(1)).generateAndStoreChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);
        verify(optionsFactory, times(1)).createAuthenticationOptions(
                eq(challenge), eq("example.com"), eq(credentials));
    }

    @Test
    @DisplayName("인증 성공")
    void authenticate_Success() {
        // given
        String credentialId = "credentialId";
        String authenticatorDataBase64 = "dGVzdA==";
        String clientDataJSONBase64 = "dGVzdA==";
        String signatureBase64 = "dGVzdA==";
        String userHandle = "dGVzdA==";
        byte[] authenticatorDataBytes = new byte[]{1, 2, 3};
        byte[] clientDataJSONBytes = new byte[]{1, 2, 3};
        byte[] signatureBytes = new byte[]{1, 2, 3};
        byte[] publicKeyCoseBytes = new byte[]{1, 2, 3};
        Origin origin = new Origin("https://example.com");
        ServerProperty serverProperty = mock(ServerProperty.class);

        when(credentialRepository.findByCredentialId(credentialId))
                .thenReturn(Optional.of(credential));
        when(challengeService.getChallenge("sessionId", WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION))
                .thenReturn(challenge);
        when(verifierPort.createServerProperty(eq(origin), eq("example.com"), eq(challenge)))
                .thenReturn(serverProperty);
        doNothing().when(verifierPort).verifyAuthentication(
                any(byte[].class), any(byte[].class), any(byte[].class),
                any(byte[].class), eq(serverProperty), any());
        doNothing().when(challengeService).removeChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);
        when(verifierPort.extractSignCount(any(byte[].class))).thenReturn(1L);
        doNothing().when(credentialDomainService).validateAndUpdateCounter(any(WebAuthnCredential.class), eq(1L));
        when(credentialRepository.save(any(WebAuthnCredential.class))).thenReturn(credential);

        // when
        User result = authenticationUseCase.authenticate(
                credentialId, authenticatorDataBase64, clientDataJSONBase64, signatureBase64, userHandle, session);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("testuser");
        verify(verifierPort, times(1)).verifyAuthentication(
                any(byte[].class), any(byte[].class), any(byte[].class),
                any(byte[].class), eq(serverProperty), any());
        verify(challengeService, times(1)).removeChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);
        verify(credentialRepository, times(1)).save(any(WebAuthnCredential.class));
    }

    @Test
    @DisplayName("인증 실패 - 인증서 없음")
    void authenticate_Fail_CredentialNotFound() {
        // given
        String credentialId = "nonExistentId";
        when(credentialRepository.findByCredentialId(credentialId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(
                credentialId, "dGVzdA==", "dGVzdA==", "dGVzdA==", null, session))
                .isInstanceOf(CredentialNotFoundException.class);

        verify(verifierPort, never()).verifyAuthentication(
                any(byte[].class), any(byte[].class), any(byte[].class),
                any(byte[].class), any(), any());
    }

    @Test
    @DisplayName("인증 실패 - 챌린지 만료")
    void authenticate_Fail_ChallengeExpired() {
        // given
        String credentialId = "credentialId";
        when(credentialRepository.findByCredentialId(credentialId))
                .thenReturn(Optional.of(credential));
        when(challengeService.getChallenge("sessionId", WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(
                credentialId, "dGVzdA==", "dGVzdA==", "dGVzdA==", null, session))
                .isInstanceOf(ChallengeExpiredException.class)
                .hasMessageContaining("챌린지를 찾을 수 없거나 만료되었습니다");

        verify(verifierPort, never()).verifyAuthentication(
                any(byte[].class), any(byte[].class), any(byte[].class),
                any(byte[].class), any(), any());
    }

    @Test
    @DisplayName("인증 실패 - 검증 실패")
    void authenticate_Fail_VerificationFailed() {
        // given
        String credentialId = "credentialId";
        Origin origin = new Origin("https://example.com");
        ServerProperty serverProperty = mock(ServerProperty.class);

        when(credentialRepository.findByCredentialId(credentialId))
                .thenReturn(Optional.of(credential));
        when(challengeService.getChallenge("sessionId", WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION))
                .thenReturn(challenge);
        when(verifierPort.createServerProperty(eq(origin), eq("example.com"), eq(challenge)))
                .thenReturn(serverProperty);
        doThrow(new WebAuthnException("검증 실패"))
                .when(verifierPort).verifyAuthentication(
                        any(byte[].class), any(byte[].class), any(byte[].class),
                        any(byte[].class), eq(serverProperty), any());

        // when & then
        assertThatThrownBy(() -> authenticationUseCase.authenticate(
                credentialId, "dGVzdA==", "dGVzdA==", "dGVzdA==", null, session))
                .isInstanceOf(WebAuthnException.class);

        verify(credentialRepository, never()).save(any(WebAuthnCredential.class));
    }
}

