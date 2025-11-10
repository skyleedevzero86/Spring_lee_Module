package com.sleekydz86.passykey.global.constants;

public final class WebAuthnConstants {
    private WebAuthnConstants() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static final String CHALLENGE_TYPE_REGISTRATION = "registration";
    public static final String CHALLENGE_TYPE_AUTHENTICATION = "authentication";
    public static final long CHALLENGE_TIMEOUT_MS = 300000L; // 5 minutes
    public static final long CREDENTIAL_COUNTER_INITIAL = 0L;
    public static final String TRANSPORT_SEPARATOR = ",";
}

