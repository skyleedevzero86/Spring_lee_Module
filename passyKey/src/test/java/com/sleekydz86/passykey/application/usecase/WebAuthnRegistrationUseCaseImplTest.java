package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.outbound.ChallengeServicePort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnConfigPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnCredentialRepositoryPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnOptionsFactoryPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnVerifierPort;
import com.sleekydz86.passykey.global.constants.WebAuthnConstants;
import com.sleekydz86.passykey.global.exception.ChallengeExpiredException;
import com.sleekydz86.passykey.global.exception.WebAuthnException;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WebAuthnRegistrationUseCaseImpl 테스트")
class WebAuthnRegistrationUseCaseImplTest {

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
    private HttpSession session;

    @InjectMocks
    private WebAuthnRegistrationUseCaseImpl registrationUseCase;

    private User user;
    private Challenge challenge;
    private PublicKeyCredentialCreationOptions options;

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

        challenge = new DefaultChallenge(new byte[]{1, 2, 3, 4});
        options = mock(PublicKeyCredentialCreationOptions.class);

        when(session.getId()).thenReturn("sessionId");
        when(configPort.getRpId()).thenReturn("example.com");
        when(configPort.getRpName()).thenReturn("Example");
        when(configPort.getAllowedOrigins()).thenReturn("https://example.com");
    }

    @Test
    @DisplayName("등록 옵션 생성 성공")
    void createRegistrationOptions_Success() {
        // given
        when(challengeService.generateAndStoreChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION))
                .thenReturn(challenge);
        when(optionsFactory.createRegistrationOptions(
                eq(user), eq(challenge), eq("example.com"), eq("Example")))
                .thenReturn(options);

        // when
        PublicKeyCredentialCreationOptions result = registrationUseCase.createRegistrationOptions(user, session);

        // then
        assertThat(result).isNotNull();
        verify(challengeService, times(1)).generateAndStoreChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION);
        verify(optionsFactory, times(1)).createRegistrationOptions(
                eq(user), eq(challenge), eq("example.com"), eq("Example"));
    }

    @Test
    @DisplayName("인증서 등록 성공")
    void registerCredential_Success() {
        // given
        String credentialId = "credentialId";
        String attestationObjectBase64 = "dGVzdA==";
        String clientDataJSONBase64 = "dGVzdA==";
        String[] transports = new String[]{"usb", "nfc"};
        byte[] publicKeyCose = new byte[]{1, 2, 3};
        Origin origin = new Origin("https://example.com");
        ServerProperty serverProperty = mock(ServerProperty.class);

        when(challengeService.getChallenge("sessionId", WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION))
                .thenReturn(challenge);
        when(verifierPort.extractPublicKeyCose(any(byte[].class))).thenReturn(publicKeyCose);
        when(verifierPort.createServerProperty(eq(origin), eq("example.com"), eq(challenge)))
                .thenReturn(serverProperty);
        doNothing().when(verifierPort).verifyRegistration(
                any(byte[].class), any(byte[].class), eq(serverProperty));
        doNothing().when(challengeService).removeChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION);
        doNothing().when(credentialRepository).save(any(WebAuthnCredential.class));

        // when
        registrationUseCase.registerCredential(
                user, credentialId, attestationObjectBase64, clientDataJSONBase64, transports, session);

        // then
        verify(verifierPort, times(1)).verifyRegistration(
                any(byte[].class), any(byte[].class), eq(serverProperty));
        verify(challengeService, times(1)).removeChallenge(
                "sessionId", WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION);
        verify(credentialRepository, times(1)).save(any(WebAuthnCredential.class));
    }

    @Test
    @DisplayName("인증서 등록 실패 - 챌린지 만료")
    void registerCredential_Fail_ChallengeExpired() {
        // given
        String credentialId = "credentialId";
        String attestationObjectBase64 = "dGVzdA==";
        String clientDataJSONBase64 = "dGVzdA==";
        String[] transports = new String[]{"usb", "nfc"};

        when(challengeService.getChallenge("sessionId", WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> registrationUseCase.registerCredential(
                user, credentialId, attestationObjectBase64, clientDataJSONBase64, transports, session))
                .isInstanceOf(ChallengeExpiredException.class)
                .hasMessageContaining("챌린지를 찾을 수 없거나 만료되었습니다");

        verify(credentialRepository, never()).save(any(WebAuthnCredential.class));
    }

    @Test
    @DisplayName("인증서 등록 실패 - 검증 실패")
    void registerCredential_Fail_VerificationFailed() {
        // given
        String credentialId = "credentialId";
        String attestationObjectBase64 = "dGVzdA==";
        String clientDataJSONBase64 = "dGVzdA==";
        String[] transports = new String[]{"usb", "nfc"};
        byte[] publicKeyCose = new byte[]{1, 2, 3};
        Origin origin = new Origin("https://example.com");
        ServerProperty serverProperty = mock(ServerProperty.class);

        when(challengeService.getChallenge("sessionId", WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION))
                .thenReturn(challenge);
        when(verifierPort.extractPublicKeyCose(any(byte[].class))).thenReturn(publicKeyCose);
        when(verifierPort.createServerProperty(eq(origin), eq("example.com"), eq(challenge)))
                .thenReturn(serverProperty);
        doThrow(new WebAuthnException("검증 실패"))
                .when(verifierPort).verifyRegistration(
                        any(byte[].class), any(byte[].class), eq(serverProperty));

        // when & then
        assertThatThrownBy(() -> registrationUseCase.registerCredential(
                user, credentialId, attestationObjectBase64, clientDataJSONBase64, transports, session))
                .isInstanceOf(WebAuthnException.class);

        verify(credentialRepository, never()).save(any(WebAuthnCredential.class));
    }
}

