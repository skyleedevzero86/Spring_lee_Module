package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnAuthenticationUseCase;
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
import com.sleekydz86.passykey.global.util.Base64UrlConverter;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.sleekydz86.passykey.adapter.outbound.webauthn.RegisteredCredential;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WebAuthnAuthenticationUseCaseImpl implements WebAuthnAuthenticationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(WebAuthnAuthenticationUseCaseImpl.class);

    private final WebAuthnCredentialRepositoryPort credentialRepository;
    private final ChallengeServicePort challengeService;
    private final WebAuthnOptionsFactoryPort optionsFactory;
    private final WebAuthnVerifierPort verifierPort;
    private final WebAuthnConfigPort configPort;
    private final CredentialDomainService credentialDomainService;

    public WebAuthnAuthenticationUseCaseImpl(
            WebAuthnCredentialRepositoryPort credentialRepository,
            ChallengeServicePort challengeService,
            WebAuthnOptionsFactoryPort optionsFactory,
            WebAuthnVerifierPort verifierPort,
            WebAuthnConfigPort configPort,
            CredentialDomainService credentialDomainService) {
        this.credentialRepository = credentialRepository;
        this.challengeService = challengeService;
        this.optionsFactory = optionsFactory;
        this.verifierPort = verifierPort;
        this.configPort = configPort;
        this.credentialDomainService = credentialDomainService;
    }

    @Override
    public PublicKeyCredentialRequestOptions createAuthenticationOptions(User user, HttpSession session) {
        String sessionId = session.getId();
        Challenge challenge = challengeService.generateAndStoreChallenge(
                sessionId, WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);

        List<WebAuthnCredential> credentials = credentialRepository.findByUser(user);
        return optionsFactory.createAuthenticationOptions(challenge, configPort.getRpId(), credentials);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User authenticate(String credentialId, String authenticatorDataBase64,
            String clientDataJSONBase64, String signatureBase64,
            String userHandle, HttpSession session) {
        try {
            WebAuthnCredential credential = credentialRepository.findByCredentialId(credentialId)
                    .orElseThrow(() -> new CredentialNotFoundException(credentialId));

            byte[] authenticatorDataBytes;
            byte[] clientDataJSONBytes;
            byte[] signatureBytes;
            byte[] publicKeyCoseBytes;

            logger.debug("=== Authentication Debug ===");
            logger.debug("credentialId: {}", credentialId);
            logger.debug("clientDataJSONBase64 length: {}",
                    clientDataJSONBase64 != null ? clientDataJSONBase64.length() : 0);
            logger.debug("clientDataJSONBase64 (first 100 chars): {}",
                    clientDataJSONBase64 != null && clientDataJSONBase64.length() > 100
                            ? clientDataJSONBase64.substring(0, 100)
                            : clientDataJSONBase64);

            try {
                authenticatorDataBytes = Base64UrlConverter.decode(authenticatorDataBase64);
                clientDataJSONBytes = Base64UrlConverter.decode(clientDataJSONBase64);
                signatureBytes = Base64UrlConverter.decode(signatureBase64);
                publicKeyCoseBytes = Base64UrlConverter.decode(credential.getPublicKeyCose());
            } catch (Exception e) {
                logger.error("Base64 디코딩 실패", e);
                logger.error("clientDataJSONBase64: {}", clientDataJSONBase64);
                throw new WebAuthnException("Base64 디코딩 실패: " + e.getMessage());
            }

            if (clientDataJSONBytes == null || clientDataJSONBytes.length == 0) {
                throw new WebAuthnException("clientDataJSON이 비어있습니다");
            }

            try {
                String clientDataJSONString = new String(clientDataJSONBytes, java.nio.charset.StandardCharsets.UTF_8);
                logger.debug("Decoded clientDataJSON: {}", clientDataJSONString);
                if (!clientDataJSONString.trim().startsWith("{")) {
                    logger.error("clientDataJSON이 유효한 JSON이 아닙니다. 첫 문자: '{}'",
                            clientDataJSONString.length() > 0 ? clientDataJSONString.charAt(0) : "empty");
                    throw new WebAuthnException("clientDataJSON이 유효한 JSON 형식이 아닙니다");
                }
            } catch (Exception e) {
                logger.error("clientDataJSON 검증 실패", e);
                logger.error("clientDataJSONBytes length: {}", clientDataJSONBytes.length);
                logger.error("clientDataJSONBytes (first 20 bytes): {}",
                        java.util.Arrays.toString(java.util.Arrays.copyOf(clientDataJSONBytes,
                                Math.min(20, clientDataJSONBytes.length))));
                throw new WebAuthnException("clientDataJSON 검증 실패: " + e.getMessage());
            }

            String sessionId = session.getId();
            Challenge challenge = challengeService.getChallenge(
                    sessionId, WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);

            if (challenge == null) {
                throw new ChallengeExpiredException("챌린지를 찾을 수 없거나 만료되었습니다");
            }

            Origin origin = new Origin(configPort.getAllowedOrigins());
            ServerProperty serverProperty = verifierPort.createServerProperty(origin, configPort.getRpId(), challenge);

            RegisteredCredential registeredCredential = new RegisteredCredential(
                    Base64UrlConverter.decode(credentialId),
                    publicKeyCoseBytes,
                    credential.getCounter(),
                    null);

            verifierPort.verifyAuthentication(
                    authenticatorDataBytes,
                    clientDataJSONBytes,
                    signatureBytes,
                    userHandle != null ? Base64UrlConverter.decode(userHandle) : null,
                    serverProperty,
                    registeredCredential);

            challengeService.removeChallenge(sessionId, WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);

            updateCredentialCounter(credential, authenticatorDataBytes);

            logger.info("사용자 인증 성공: {}", credential.getUser().getUsername());
            return credential.getUser();
        } catch (WebAuthnException e) {
            throw e;
        } catch (Exception e) {
            logger.error("인증 실패", e);
            throw new WebAuthnException("인증 실패: " + e.getMessage(), e);
        }
    }

    private void updateCredentialCounter(WebAuthnCredential credential, byte[] authenticatorDataBytes) {
        try {
            long newCounter = verifierPort.extractSignCount(authenticatorDataBytes);
            credentialDomainService.validateAndUpdateCounter(credential, newCounter);
            credentialRepository.save(credential);
        } catch (Exception e) {
            throw new WebAuthnException("인증서 카운터 업데이트 실패", e);
        }
    }
}
