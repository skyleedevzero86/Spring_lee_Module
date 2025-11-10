package com.sleekydz86.passykey.global.exception;

public class CredentialNotFoundException extends WebAuthnException {
    public CredentialNotFoundException(String credentialId) {
        super("인증서를 찾을 수 없습니다: " + credentialId);
    }
}
