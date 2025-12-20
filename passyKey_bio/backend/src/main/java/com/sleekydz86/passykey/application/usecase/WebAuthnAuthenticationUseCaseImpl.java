package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.domain.model.AuthenticationResult;
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
import com.sleekydz86.passykey.global.util.ClientDataJSONParser;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.authenticator.Authenticator;
import com.webauthn4j.authenticator.AuthenticatorImpl;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.authenticator.AAGUID;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.attestation.authenticator.COSEKey;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
    public PublicKeyCredentialRequestOptions createAuthenticationOptions(User user, HttpSession session, String rpId) {
        String sessionId = session.getId();
        Challenge challenge = challengeService.generateAndStoreChallenge(
                sessionId, WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);

        List<WebAuthnCredential> credentials = credentialRepository.findByUser(user);
        String effectiveRpId = determineRpId(rpId);
        return optionsFactory.createAuthenticationOptions(challenge, effectiveRpId, credentials);
    }

    private String determineRpId(String requestHost) {
        if (requestHost != null && !requestHost.isEmpty()) {
            if (requestHost.contains(".ngrok.io") || requestHost.contains(".ngrok-free.app")) {
                return requestHost;
            }
            if (!requestHost.equals("localhost") && !requestHost.startsWith("localhost:")) {
                return requestHost;
            }
        }
        return configPort.getRpId();
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

            logger.debug("=== 인증 디버그 ===");
            logger.debug("credentialId: {}", credentialId);
            logger.debug("clientDataJSONBase64 길이: {}",
                    clientDataJSONBase64 != null ? clientDataJSONBase64.length() : 0);
            logger.debug("clientDataJSONBase64 (처음 100자): {}",
                    clientDataJSONBase64 != null && clientDataJSONBase64.length() > 100
                            ? clientDataJSONBase64.substring(0, 100)
                            : clientDataJSONBase64);

            Base64.Decoder urlDecoder = Base64.getUrlDecoder();
            try {
                authenticatorDataBytes = urlDecoder.decode(authenticatorDataBase64);
                clientDataJSONBytes = urlDecoder.decode(clientDataJSONBase64);
                signatureBytes = urlDecoder.decode(signatureBase64);
                publicKeyCoseBytes = urlDecoder.decode(credential.getPublicKeyCose());
            } catch (IllegalArgumentException e) {
                logger.error("Base64 디코딩 실패", e);
                logger.error("clientDataJSONBase64: {}", clientDataJSONBase64);
                throw new WebAuthnException("Base64 디코딩 실패: " + e.getMessage());
            } catch (Exception e) {
                logger.error("Base64 디코딩 중 예외 발생", e);
                logger.error("clientDataJSONBase64: {}", clientDataJSONBase64);
                throw new WebAuthnException("Base64 디코딩 실패: " + e.getMessage());
            }

            if (clientDataJSONBytes == null || clientDataJSONBytes.length == 0) {
                throw new WebAuthnException("clientDataJSON이 비어있습니다");
            }

            String clientDataJSONString = new String(clientDataJSONBytes, StandardCharsets.UTF_8);
            logger.info("=== 디코딩된 clientDataJSON ===");
            logger.info("clientDataJSON: {}", clientDataJSONString);
            logger.info("clientDataBytes 길이: {}", clientDataJSONBytes.length);
            logger.info("처음 20바이트: {}", java.util.Arrays.toString(
                    java.util.Arrays.copyOf(clientDataJSONBytes, Math.min(20, clientDataJSONBytes.length))));

            if (!clientDataJSONString.trim().startsWith("{")) {
                logger.error("clientDataJSON이 유효한 JSON이 아닙니다. 첫 문자: '{}', length: {}",
                        clientDataJSONString.length() > 0 ? clientDataJSONString.charAt(0) : "empty",
                        clientDataJSONBytes.length);
                logger.error("clientDataJSONBytes (처음 50바이트): {}",
                        java.util.Arrays.toString(java.util.Arrays.copyOf(clientDataJSONBytes,
                                Math.min(50, clientDataJSONBytes.length))));
                throw new WebAuthnException("clientDataJSON이 유효한 JSON 형식이 아닙니다");
            }

            String sessionId = session.getId();
            Challenge challenge = challengeService.getChallenge(
                    sessionId, WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);

            if (challenge == null) {
                throw new ChallengeExpiredException("챌린지를 찾을 수 없거나 만료되었습니다");
            }

            Origin origin = ClientDataJSONParser.extractOrigin(clientDataJSONBytes);
            validateOrigin(origin);
            String originHost = ClientDataJSONParser.extractOriginString(clientDataJSONBytes);
            String effectiveRpId = determineRpId(originHost);
            ServerProperty serverProperty = verifierPort.createServerProperty(origin, effectiveRpId, challenge);

            byte[] credentialIdBytes = urlDecoder.decode(credentialId);
            ObjectConverter objectConverter = new ObjectConverter();
            COSEKey coseKey = objectConverter.getCborConverter().readValue(publicKeyCoseBytes, COSEKey.class);

            AttestedCredentialData attestedCredentialData = new AttestedCredentialData(
                    AAGUID.ZERO,
                    credentialIdBytes,
                    coseKey);

            Authenticator authenticator = new AuthenticatorImpl(
                    attestedCredentialData,
                    null,
                    credential.getCounter());

            AuthenticationResult authResult = verifierPort.verifyAuthentication(
                    authenticatorDataBytes,
                    clientDataJSONBytes,
                    signatureBytes,
                    userHandle != null ? urlDecoder.decode(userHandle) : null,
                    serverProperty,
                    authenticator);

            challengeService.removeChallenge(sessionId, WebAuthnConstants.CHALLENGE_TYPE_AUTHENTICATION);

            if (credential.getId() == null) {
                throw new WebAuthnException("인증서 ID가 없습니다. 인증서를 다시 등록해주세요.");
            }

            credentialDomainService.validateAndUpdateCounter(credential, authResult.getCounter());
            credential.setLastUsedAt(java.time.LocalDateTime.now());
            credentialRepository.update(credential);

            logger.info("사용자 인증 성공: {}", credential.getUser().getUsername());
            return credential.getUser();
        } catch (WebAuthnException e) {
            throw e;
        } catch (Exception e) {
            logger.error("인증 실패", e);
            throw new WebAuthnException("인증 실패: " + e.getMessage(), e);
        }
    }

    private void validateOrigin(Origin origin) {
        String allowedOrigins = configPort.getAllowedOrigins();
        String originString = origin.toString();
        
        if (allowedOrigins == null || allowedOrigins.isEmpty()) {
            return;
        }
        
        if (originString.contains(".ngrok.io") || originString.contains(".ngrok-free.app")) {
            return;
        }
        
        String[] allowedOriginsArray = allowedOrigins.split(",");
        for (String allowed : allowedOriginsArray) {
            String trimmed = allowed.trim();
            if (originString.equals(trimmed) || 
                originString.equals(trimmed + "/") ||
                (trimmed.endsWith("/") && originString.equals(trimmed.substring(0, trimmed.length() - 1)))) {
                return;
            }
        }
        
        throw new WebAuthnException("잘못된 CORS 요청: Origin " + originString + "이(가) 허용되지 않습니다");
    }

}
