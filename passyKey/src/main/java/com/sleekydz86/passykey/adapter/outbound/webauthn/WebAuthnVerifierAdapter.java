package com.sleekydz86.passykey.adapter.outbound.webauthn;

import com.sleekydz86.passykey.domain.port.outbound.WebAuthnVerifierPort;
import com.webauthn4j.converter.util.ObjectConverter;
import com.webauthn4j.data.attestation.AttestationObject;
import com.webauthn4j.data.attestation.authenticator.AuthenticatorData;
import com.sleekydz86.passykey.adapter.outbound.webauthn.RegisteredCredential;
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
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebAuthnVerifierAdapter implements WebAuthnVerifierPort {

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
            throw new RuntimeException("Registration validation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void verifyAuthentication(byte[] authenticatorDataBytes, byte[] clientDataJSONBytes,
            byte[] signatureBytes, byte[] userHandle, ServerProperty serverProperty,
            RegisteredCredential registeredCredential) {
        AuthenticationRequest authenticationRequest = new AuthenticationRequest(
                registeredCredential.getCredentialId(),
                authenticatorDataBytes,
                clientDataJSONBytes,
                signatureBytes,
                userHandle);

        AuthenticationParameters authenticationParameters = new AuthenticationParameters(
                serverProperty,
                registeredCredential,
                null,
                false,
                true);

        try {
            webAuthnManager.validate(authenticationRequest, authenticationParameters);
        } catch (WebAuthnException e) {
            throw new RuntimeException("Authentication validation failed: " + e.getMessage(), e);
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

    @Override
    public long extractSignCount(byte[] authenticatorDataBytes) {
        AuthenticatorData<?> authenticatorData = objectConverter.getCborConverter()
                .readValue(authenticatorDataBytes, AuthenticatorData.class);
        return authenticatorData.getSignCount();
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
