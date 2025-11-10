package com.sleekydz86.passykey.adapter.outbound.webauthn;

import com.sleekydz86.passykey.domain.port.outbound.WebAuthnVerifierPort;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.webauthn4j.data.attestation.authenticator.RegisteredCredential;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;
import com.webauthn4j.verifier.WebAuthnAuthenticationContextVerifier;
import com.webauthn4j.verifier.WebAuthnRegistrationContextVerifier;
import org.springframework.stereotype.Component;

@Component
public class WebAuthnVerifierAdapter implements WebAuthnVerifierPort {

    private final ObjectConverter objectConverter;
    private final WebAuthnRegistrationContextVerifier registrationVerifier;
    private final WebAuthnAuthenticationContextVerifier authenticationVerifier;

    public WebAuthnVerifierAdapter() {
        this.objectConverter = new ObjectConverter();
        this.registrationVerifier = new WebAuthnRegistrationContextVerifier(objectConverter);
        this.authenticationVerifier = new WebAuthnAuthenticationContextVerifier(objectConverter);
    }

    @Override
    public void verifyRegistration(byte[] attestationObjectBytes, byte[] clientDataJSONBytes, ServerProperty serverProperty) {
        com.webauthn4j.verifier.WebAuthnRegistrationContext registrationContext =
                new com.webauthn4j.verifier.WebAuthnRegistrationContext(
                        attestationObjectBytes,
                        clientDataJSONBytes,
                        null,
                        serverProperty,
                        false,
                        true
                );

        registrationVerifier.verify(registrationContext);
    }

    @Override
    public void verifyAuthentication(byte[] authenticatorDataBytes, byte[] clientDataJSONBytes,
                                     byte[] signatureBytes, byte[] userHandle, ServerProperty serverProperty,
                                     RegisteredCredential registeredCredential) {
        com.webauthn4j.data.AuthenticatorAssertionResponse response = new com.webauthn4j.data.AuthenticatorAssertionResponse(
                authenticatorDataBytes,
                clientDataJSONBytes,
                signatureBytes,
                userHandle
        );

        com.webauthn4j.verifier.WebAuthnAuthenticationContext authenticationContext =
                new com.webauthn4j.verifier.WebAuthnAuthenticationContext(
                        response,
                        serverProperty,
                        registeredCredential
                );

        authenticationVerifier.verify(authenticationContext);
    }

    @Override
    public ServerProperty createServerProperty(Origin origin, String rpId, Challenge challenge) {
        return new ServerProperty(
                origin,
                rpId,
                challenge,
                null,
                false
        );
    }

    @Override
    public byte[] extractPublicKeyCose(byte[] attestationObjectBytes) {
        AttestationObject attestationObject = objectConverter.getCborConverter()
                .readValue(attestationObjectBytes, AttestationObject.class);
        return attestationObject.getAuthenticatorData()
                .getAttestedCredentialData().getCOSEKey().getBytes();
    }

    @Override
    public long extractSignCount(byte[] authenticatorDataBytes) {
        AuthenticatorData<RegisteredCredential> authenticatorData = objectConverter.getCborConverter()
                .readValue(authenticatorDataBytes, AuthenticatorData.class);
        return authenticatorData.getSignCount();
    }
}

