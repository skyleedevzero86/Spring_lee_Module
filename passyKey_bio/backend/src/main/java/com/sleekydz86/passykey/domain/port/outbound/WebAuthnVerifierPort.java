package com.sleekydz86.passykey.domain.port.outbound;

import com.sleekydz86.passykey.domain.model.AuthenticationResult;
import com.webauthn4j.authenticator.Authenticator;
import com.webauthn4j.data.client.Origin;
import com.webauthn4j.data.client.challenge.Challenge;
import com.webauthn4j.server.ServerProperty;

public interface WebAuthnVerifierPort {
    void verifyRegistration(byte[] attestationObjectBytes, byte[] clientDataJSONBytes, ServerProperty serverProperty);

    AuthenticationResult verifyAuthentication(byte[] authenticatorDataBytes, byte[] clientDataJSONBytes,
            byte[] signatureBytes, byte[] userHandle, ServerProperty serverProperty,
            Authenticator authenticator);

    ServerProperty createServerProperty(Origin origin, String rpId, Challenge challenge);

    byte[] extractPublicKeyCose(byte[] attestationObjectBytes);
}
