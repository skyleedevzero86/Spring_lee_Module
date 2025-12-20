package com.sleekydz86.passykey.global.exception;

public class WebAuthnException extends RuntimeException {
    public WebAuthnException(String message) {
        super(message);
    }

    public WebAuthnException(String message, Throwable cause) {
        super(message, cause);
    }
}
