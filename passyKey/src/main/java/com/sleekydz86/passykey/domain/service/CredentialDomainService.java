package com.sleekydz86.passykey.domain.service;

import com.sleekydz86.passykey.domain.model.WebAuthnCredential;
import com.sleekydz86.passykey.global.exception.InvalidCounterException;

public class CredentialDomainService {

    public void validateAndUpdateCounter(WebAuthnCredential credential, Long newCounter) {
        if (newCounter <= credential.getCounter()) {
            throw new InvalidCounterException("카운터는 현재 값보다 커야 합니다");
        }
        credential.updateCounter(newCounter);
    }
}

