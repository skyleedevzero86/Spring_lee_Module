package com.sleekydz86.passykey.domain.port.outbound;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import com.webauthn4j.data.client.challenge.Challenge;

import java.util.List;

public interface WebAuthnOptionsFactoryPort {
    PublicKeyCredentialCreationOptions createRegistrationOptions(User user, Challenge challenge, String rpId, String rpName);
    PublicKeyCredentialRequestOptions createAuthenticationOptions(Challenge challenge, String rpId, List<WebAuthnCredential> credentials);
}




