package com.sleekydz86.passykey.adapter.outbound.webauthn;

import com.sleekydz86.passykey.domain.model.AuthenticationResult;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnVerifierPort;
import com.webauthn4j.authenticator.Authenticator;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.WebAuthnManager;
import com.webauthn4j.data.RegistrationParameters;
import com.webauthn4j.data.AuthenticationParameters;
import com.webauthn4j.data.RegistrationRequest;
import com.webauthn4j.data.AuthenticationRequest;
import com.webauthn4j.data.PublicKeyCredentialParameters;
import com.webauthn4j.data.PublicKeyCredentialType;
import com.webauthn4j.util.exception.WebAuthnException;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebAuthnVerifierAdapter implements WebAuthnVerifierPort {

        private static final Logger logger = LoggerFactory.getLogger(WebAuthnVerifierAdapter.class);

        private final ObjectConverter objectConverter;
        private final WebAuthnManager webAuthnManager;

        public WebAuthnVerifierAdapter() {
                this.objectConverter = new ObjectConverter();
                this.webAuthnManager = WebAuthnManager.createNonStrictWebAuthnManager();
        }

        @Override
        public void verifyRegistration(byte[] attestationObjectBytes, byte[] clientDataJSONBytes,
                        ServerProperty serverProperty) {
                RegistrationRequest registrationRequest = new RegistrationRequest(
                                attestationObjectBytes,
                                clientDataJSONBytes,
                                null,
                                null);

                List<PublicKeyCredentialParameters> allowedParameters = getAllowedParameters();
                RegistrationParameters registrationParameters = new RegistrationParameters(
                                serverProperty,
                                allowedParameters,
                                false,
                                true);

                try {
                        webAuthnManager.validate(registrationRequest, registrationParameters);
                } catch (WebAuthnException e) {
                        String errorMessage = translateErrorMessage(e.getMessage());
                        throw new RuntimeException("등록 검증 실패: " + errorMessage, e);
                }
        }

        @Override
        public AuthenticationResult verifyAuthentication(byte[] authenticatorDataBytes, byte[] clientDataJSONBytes,
                        byte[] signatureBytes, byte[] userHandle, ServerProperty serverProperty,
                        Authenticator authenticator) {
                if (clientDataJSONBytes == null || clientDataJSONBytes.length == 0) {
                        throw new RuntimeException("clientDataJSON이 null이거나 비어있습니다");
                }

                String clientDataJSONString = new String(clientDataJSONBytes, StandardCharsets.UTF_8);
                logger.info("수신된 clientDataJSON (디코딩됨): {}", clientDataJSONString);
                logger.info("clientDataJSONBytes 길이: {}", clientDataJSONBytes.length);
                logger.info("처음 20바이트: {}", java.util.Arrays.toString(
                                java.util.Arrays.copyOf(clientDataJSONBytes,
                                                Math.min(20, clientDataJSONBytes.length))));

                AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                                authenticator.getAttestedCredentialData().getCredentialId(),
                                userHandle,
                                authenticatorDataBytes,
                                clientDataJSONBytes,
                                null,
                                signatureBytes);

                AuthenticationParameters authenticationParameters = new AuthenticationParameters(
                                serverProperty,
                                authenticator,
                                null,
                                false,
                                true);

                try {
                        if (serverProperty.getChallenge() != null) {
                                String expectedChallenge = com.webauthn4j.util.Base64UrlUtil.encodeToString(
                                                serverProperty.getChallenge().getValue());
                                logger.debug("예상되는 Challenge: {}", expectedChallenge);
                                
                                try {
                                        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                        com.fasterxml.jackson.databind.JsonNode clientDataJson = objectMapper.readTree(clientDataJSONString);
                                        if (clientDataJson.has("challenge")) {
                                                String actualChallenge = clientDataJson.get("challenge").asText();
                                                logger.debug("실제 Challenge (clientDataJSON에서): {}", actualChallenge);
                                                if (!expectedChallenge.equals(actualChallenge)) {
                                                        logger.error("Challenge 불일치! 예상: {}, 실제: {}", expectedChallenge, actualChallenge);
                                                }
                                        }
                                } catch (Exception e) {
                                        logger.debug("clientDataJSON에서 challenge 추출 실패", e);
                                }
                        }
                        
                        webAuthnManager.validate(authenticationRequest, authenticationParameters);
                        logger.info("인증 검증 성공");

                        long newSignCount = extractSignCountFromBytes(authenticatorDataBytes);
                        logger.info("새로운 서명 카운터: {}", newSignCount);

                        String credentialIdBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(
                                        authenticator.getAttestedCredentialData().getCredentialId());

                        return AuthenticationResult.builder()
                                        .credentialId(credentialIdBase64)
                                        .counter(newSignCount)
                                        .build();
                } catch (WebAuthnException e) {
                        logger.error("WebAuthn 검증 실패", e);
                        String errorMessage = translateErrorMessage(e.getMessage());
                        throw new RuntimeException("인증 검증 실패: " + errorMessage, e);
                }
        }

        @Override
        public ServerProperty createServerProperty(Origin origin, String rpId, Challenge challenge) {
                return new ServerProperty(
                                origin,
                                rpId,
                                challenge);
        }

        @Override
        public byte[] extractPublicKeyCose(byte[] attestationObjectBytes) {
                AttestationObject attestationObject = objectConverter.getCborConverter()
                                .readValue(attestationObjectBytes, AttestationObject.class);
                return objectConverter.getCborConverter()
                                .writeValueAsBytes(attestationObject.getAuthenticatorData()
                                                .getAttestedCredentialData().getCOSEKey());
        }

        private long extractSignCountFromBytes(byte[] authenticatorDataBytes) {
                if (authenticatorDataBytes == null || authenticatorDataBytes.length < 37) {
                        logger.warn("잘못된 authenticatorDataBytes 길이: {}",
                                        authenticatorDataBytes != null ? authenticatorDataBytes.length : 0);
                        return 0;
                }

                long signCount = ((authenticatorDataBytes[33] & 0xFFL) << 24) |
                                ((authenticatorDataBytes[34] & 0xFFL) << 16) |
                                ((authenticatorDataBytes[35] & 0xFFL) << 8) |
                                (authenticatorDataBytes[36] & 0xFFL);

                return signCount;
        }

        private List<PublicKeyCredentialParameters> getAllowedParameters() {
                List<PublicKeyCredentialParameters> parameters = new ArrayList<>();
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-7)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-257)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-8)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-37)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-35)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-36)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-258)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-38)));
                parameters.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-39)));
                return parameters;
        }

        private String translateErrorMessage(String message) {
                if (message == null) {
                        return "알 수 없는 오류";
                }
                if (message.contains("challenge") || message.contains("Challenge")) {
                        return message.replace("challenge", "챌린지").replace("Challenge", "챌린지");
                }
                if (message.contains("origin") || message.contains("Origin")) {
                        return message.replace("origin", "출처").replace("Origin", "출처");
                }
                if (message.contains("signature") || message.contains("Signature")) {
                        return message.replace("signature", "서명").replace("Signature", "서명");
                }
                if (message.contains("credential") || message.contains("Credential")) {
                        return message.replace("credential", "인증서").replace("Credential", "인증서");
                }
                if (message.contains("verification") || message.contains("Verification")) {
                        return message.replace("verification", "검증").replace("Verification", "검증");
                }
                if (message.contains("validation") || message.contains("Validation")) {
                        return message.replace("validation", "유효성 검사").replace("Validation", "유효성 검사");
                }
                if (message.contains("invalid") || message.contains("Invalid")) {
                        return message.replace("invalid", "잘못된").replace("Invalid", "잘못된");
                }
                if (message.contains("failed") || message.contains("Failed")) {
                        return message.replace("failed", "실패").replace("Failed", "실패");
                }
                if (message.contains("error") || message.contains("Error")) {
                        return message.replace("error", "오류").replace("Error", "오류");
                }
                return message;
        }
}
