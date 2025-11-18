package com.sleekydz86.passykey.domain.port.inbound;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;

import java.util.List;

public interface CredentialManagementUseCase {
    List<WebAuthnCredential> getUserCredentials(User user);
    void deleteCredential(String credentialId);
}





