package com.sleekydz86.passykey.domain.port.inbound;

import com.sleekydz86.passykey.domain.model.User;
import com.webauthn4j.data.PublicKeyCredentialRequestOptions;
import jakarta.servlet.http.HttpSession;

public interface WebAuthnAuthenticationUseCase {
    PublicKeyCredentialRequestOptions createAuthenticationOptions(User user, HttpSession session);
    User authenticate(String credentialId, String authenticatorDataBase64,
                     String clientDataJSONBase64, String signatureBase64,
                     String userHandle, HttpSession session);
}


