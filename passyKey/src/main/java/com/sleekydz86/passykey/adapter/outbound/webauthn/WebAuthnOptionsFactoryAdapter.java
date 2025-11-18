package com.sleekydz86.passykey.adapter.outbound.webauthn;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.domain.port.outbound.WebAuthnOptionsFactoryPort;
import com.sleekydz86.passykey.global.constants.WebAuthnConstants;
import com.sleekydz86.passykey.global.util.Base64UrlConverter;
import com.webauthn4j.data.*;
import com.webauthn4j.data.client.challenge.Challenge;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WebAuthnOptionsFactoryAdapter implements WebAuthnOptionsFactoryPort {

        @Override
        public PublicKeyCredentialCreationOptions createRegistrationOptions(
                        User user, Challenge challenge, String rpId, String rpName) {

                byte[] userId = Base64UrlConverter.decode(user.getUserHandle());

                PublicKeyCredentialUserEntity userEntity = new PublicKeyCredentialUserEntity(
                                userId,
                                user.getUsername(),
                                user.getDisplayName());

                PublicKeyCredentialRpEntity rpEntity = new PublicKeyCredentialRpEntity(rpId, rpName);

                List<PublicKeyCredentialParameters> pubKeyCredParams = createPublicKeyCredentialParameters();

                AuthenticatorSelectionCriteria authenticatorSelection = new AuthenticatorSelectionCriteria(
                                AuthenticatorAttachment.PLATFORM,
                                true,
                                UserVerificationRequirement.PREFERRED);

                return new PublicKeyCredentialCreationOptions(
                                rpEntity,
                                userEntity,
                                challenge,
                                pubKeyCredParams,
                                (long) WebAuthnConstants.CHALLENGE_TIMEOUT_MS,
                                Collections.emptyList(),
                                authenticatorSelection,
                                AttestationConveyancePreference.NONE,
                                null);
        }

        @Override
        public PublicKeyCredentialRequestOptions createAuthenticationOptions(
                        Challenge challenge, String rpId, List<WebAuthnCredential> credentials) {

                List<PublicKeyCredentialDescriptor> allowCredentials = credentials.stream()
                                .map(this::toPublicKeyCredentialDescriptor)
                                .collect(Collectors.toList());

                return new PublicKeyCredentialRequestOptions(
                                challenge,
                                WebAuthnConstants.CHALLENGE_TIMEOUT_MS,
                                rpId,
                                allowCredentials,
                                UserVerificationRequirement.PREFERRED,
                                null);
        }

        private List<PublicKeyCredentialParameters> createPublicKeyCredentialParameters() {
                List<PublicKeyCredentialParameters> params = new ArrayList<>();
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-7)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-257)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-8)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-37)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-35)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-36)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-258)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-38)));
                params.add(new PublicKeyCredentialParameters(PublicKeyCredentialType.PUBLIC_KEY,
                                com.webauthn4j.data.attestation.statement.COSEAlgorithmIdentifier.create(-39)));
                return params;
        }

        private PublicKeyCredentialDescriptor toPublicKeyCredentialDescriptor(WebAuthnCredential cred) {
                byte[] credentialId = Base64UrlConverter.decode(cred.getCredentialId());
                Set<AuthenticatorTransport> transports = parseTransports(cred.getTransports());

                return new PublicKeyCredentialDescriptor(
                                PublicKeyCredentialType.PUBLIC_KEY,
                                credentialId,
                                transports);
        }

        private Set<AuthenticatorTransport> parseTransports(String transportsString) {
                if (transportsString == null || transportsString.isEmpty()) {
                        return Collections.emptySet();
                }
                return Arrays.stream(transportsString.split(WebAuthnConstants.TRANSPORT_SEPARATOR))
                                .map(String::trim)
                                .map(String::toUpperCase)
                                .map(name -> AuthenticatorTransport.create(name))
                                .collect(Collectors.toSet());
        }
}
