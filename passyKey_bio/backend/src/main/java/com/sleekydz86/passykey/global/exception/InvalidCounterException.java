package com.sleekydz86.passykey.global.exception;

public class InvalidCounterException extends WebAuthnException {
    public InvalidCounterException(String message) {
        super(message);
    }
}
