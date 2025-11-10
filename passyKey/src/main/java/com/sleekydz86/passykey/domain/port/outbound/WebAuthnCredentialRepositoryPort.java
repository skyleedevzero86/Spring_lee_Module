package com.sleekydz86.passykey.domain.port.outbound;

import com.sleekydz86.passykey.domain.model.User;
import com.sleekydz86.passykey.domain.model.WebAuthnCredential;

import java.util.List;
import java.util.Optional;

public interface WebAuthnCredentialRepositoryPort {
    WebAuthnCredential save(WebAuthnCredential credential);
    Optional<WebAuthnCredential> findByCredentialId(String credentialId);
    List<WebAuthnCredential> findByUser(User user);
    void deleteByCredentialId(String credentialId);
}



