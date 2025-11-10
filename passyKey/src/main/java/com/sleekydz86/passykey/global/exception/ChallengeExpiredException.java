package com.sleekydz86.passykey.global.exception;

public class ChallengeExpiredException extends WebAuthnException {
    public ChallengeExpiredException(String message) {
        super(message);
    }
}
