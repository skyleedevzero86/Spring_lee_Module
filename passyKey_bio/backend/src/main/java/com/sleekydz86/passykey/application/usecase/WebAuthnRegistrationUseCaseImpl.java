package com.sleekydz86.passykey.application.usecase;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.inbound.WebAuthnRegistrationUseCase;
import com.sleekydz86.passykey.domain.port.outbound.ChallengeServicePort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnConfigPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnCredentialRepositoryPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnOptionsFactoryPort;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnVerifierPort;
import com.sleekydz86.passykey.global.constants.WebAuthnConstants;
import com.sleekydz86.passykey.global.exception.ChallengeExpiredException;
import com.sleekydz86.passykey.global.exception.WebAuthnException;
import com.sleekydz86.passykey.global.util.Base64UrlConverter;
import com.sleekydz86.passykey.global.util.ClientDataJSONParser;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
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
public class WebAuthnRegistrationUseCaseImpl implements WebAuthnRegistrationUseCase {

    private static final Logger logger = LoggerFactory.getLogger(WebAuthnRegistrationUseCaseImpl.class);

    private final WebAuthnCredentialRepositoryPort credentialRepository;
    private final ChallengeServicePort challengeService;
    private final WebAuthnOptionsFactoryPort optionsFactory;
    private final WebAuthnVerifierPort verifierPort;
    private final WebAuthnConfigPort configPort;

    public WebAuthnRegistrationUseCaseImpl(
            WebAuthnCredentialRepositoryPort credentialRepository,
            ChallengeServicePort challengeService,
            WebAuthnOptionsFactoryPort optionsFactory,
            WebAuthnVerifierPort verifierPort,
            WebAuthnConfigPort configPort) {
        this.credentialRepository = credentialRepository;
        this.challengeService = challengeService;
        this.optionsFactory = optionsFactory;
        this.verifierPort = verifierPort;
        this.configPort = configPort;
    }

    @Override
    public PublicKeyCredentialCreationOptions createRegistrationOptions(User user, HttpSession session, String rpId) {
        String sessionId = session.getId();
        Challenge challenge = challengeService.generateAndStoreChallenge(
                sessionId, WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION);

        String effectiveRpId = determineRpId(rpId);
        return optionsFactory.createRegistrationOptions(
                user, challenge, effectiveRpId, configPort.getRpName());
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
    public void registerCredential(User user, String credentialId, String attestationObjectBase64,
                                   String clientDataJSONBase64, String[] transports, String label, HttpSession session) {
        try {
            List<WebAuthnCredential> existingCredentials = credentialRepository.findByUser(user);
            
            if (existingCredentials.size() >= 3) {
                throw new WebAuthnException("패스키는 총 3개만 만들 수 있습니다. 새로 추가하려면 기존 패스키를 삭제해주세요.");
            }
            
            if (label != null && !label.trim().isEmpty()) {
                String trimmedLabel = label.trim();
                boolean labelExists = existingCredentials.stream()
                        .anyMatch(cred -> cred.getLabel() != null && cred.getLabel().trim().equals(trimmedLabel));
                if (labelExists) {
                    throw new WebAuthnException("이미 사용 중인 패스키 이름입니다. 다른 이름을 사용해주세요.");
                }
            }

            byte[] attestationObjectBytes = Base64UrlConverter.decode(attestationObjectBase64);
            byte[] clientDataJSONBytes = Base64UrlConverter.decode(clientDataJSONBase64);

            byte[] publicKeyCose = verifierPort.extractPublicKeyCose(attestationObjectBytes);

            String sessionId = session.getId();
            Challenge challenge = challengeService.getChallenge(
                    sessionId, WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION);

            if (challenge == null) {
                throw new ChallengeExpiredException("챌린지를 찾을 수 없거나 만료되었습니다");
            }

            Origin origin = ClientDataJSONParser.extractOrigin(clientDataJSONBytes);
            validateOrigin(origin);
            String originHost = ClientDataJSONParser.extractOriginString(clientDataJSONBytes);
            String effectiveRpId = determineRpId(originHost);
            ServerProperty serverProperty = verifierPort.createServerProperty(
                    origin, effectiveRpId, challenge);

            verifierPort.verifyRegistration(attestationObjectBytes, clientDataJSONBytes, serverProperty);
            challengeService.removeChallenge(sessionId, WebAuthnConstants.CHALLENGE_TYPE_REGISTRATION);

            saveCredential(user, credentialId, publicKeyCose, transports, label);
            logger.info("사용자 인증서 등록 성공: {}", user.getUsername());
        } catch (WebAuthnException e) {
            throw e;
        } catch (Exception e) {
            logger.error("인증서 등록 실패", e);
            throw new WebAuthnException("인증서 등록 실패: " + e.getMessage(), e);
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

    private void saveCredential(User user, String credentialId, byte[] publicKeyCose, String[] transports, String label) {
        String transportsString = transports != null
                ? String.join(WebAuthnConstants.TRANSPORT_SEPARATOR, transports)
                : "";

        WebAuthnCredential credential = new WebAuthnCredential(
                credentialId,
                Base64UrlConverter.encode(publicKeyCose),
                WebAuthnConstants.CREDENTIAL_COUNTER_INITIAL,
                transportsString,
                user
        );
        
        if (label != null && !label.trim().isEmpty()) {
            credential.setLabel(label.trim());
        }

        credentialRepository.save(credential);
    }
}
