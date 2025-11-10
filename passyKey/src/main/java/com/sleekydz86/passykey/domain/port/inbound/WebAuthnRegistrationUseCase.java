package com.sleekydz86.passykey.domain.port.inbound;

import com.sleekydz86.passykey.domain.model.User;
import com.webauthn4j.data.PublicKeyCredentialCreationOptions;
import jakarta.servlet.http.HttpSession;

public interface WebAuthnRegistrationUseCase {
    PublicKeyCredentialCreationOptions createRegistrationOptions(User user, HttpSession session);
    void registerCredential(User user, String credentialId, String attestationObjectBase64,
                            String clientDataJSONBase64, String[] transports, HttpSession session);
}



