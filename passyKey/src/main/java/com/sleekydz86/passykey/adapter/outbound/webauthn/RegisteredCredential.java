package com.sleekydz86.passykey.adapter.outbound.webauthn;

import java.util.Arrays;

public class RegisteredCredential {
    private final byte[] credentialId;
    private final byte[] publicKeyCose;
    private final long counter;
    private final byte[] userHandle;

    public RegisteredCredential(byte[] credentialId, byte[] publicKeyCose, long counter, byte[] userHandle) {
        this.credentialId = credentialId != null ? Arrays.copyOf(credentialId, credentialId.length) : null;
        this.publicKeyCose = publicKeyCose != null ? Arrays.copyOf(publicKeyCose, publicKeyCose.length) : null;
        this.counter = counter;
        this.userHandle = userHandle != null ? Arrays.copyOf(userHandle, userHandle.length) : null;
    }

    public byte[] getCredentialId() {
        return credentialId != null ? Arrays.copyOf(credentialId, credentialId.length) : null;
    }

    public byte[] getPublicKeyCose() {
        return publicKeyCose != null ? Arrays.copyOf(publicKeyCose, publicKeyCose.length) : null;
    }

    public long getCounter() {
        return counter;
    }

    public byte[] getUserHandle() {
        return userHandle != null ? Arrays.copyOf(userHandle, userHandle.length) : null;
    }
}

