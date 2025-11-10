package com.sleekydz86.passykey.domain.port.outbound;

import com.webauthn4j.data.attestation.authenticator.RegisteredCredential;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;

public interface WebAuthnVerifierPort {
    void verifyRegistration(byte[] attestationObjectBytes, byte[] clientDataJSONBytes, ServerProperty serverProperty);
    void verifyAuthentication(byte[] authenticatorDataBytes, byte[] clientDataJSONBytes,
                             byte[] signatureBytes, byte[] userHandle, ServerProperty serverProperty,
                             RegisteredCredential registeredCredential);
    ServerProperty createServerProperty(Origin origin, String rpId, Challenge challenge);
    byte[] extractPublicKeyCose(byte[] attestationObjectBytes);
    long extractSignCount(byte[] authenticatorDataBytes);
}

