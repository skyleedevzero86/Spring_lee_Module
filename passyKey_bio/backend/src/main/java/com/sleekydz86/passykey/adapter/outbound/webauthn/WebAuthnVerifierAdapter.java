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
                        throw new RuntimeException("등록 검증 실패: " + e.getMessage(), e);
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
                        throw new RuntimeException("인증 검증 실패: " + e.getMessage(), e);
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
}
