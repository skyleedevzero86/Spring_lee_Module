package com.sleekydz86.passykey.adapter.outbound.webauthn;

import com.webauthn4j.credential.CredentialRecord;
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData;
import com.webauthn4j.data.client.CollectedClientData;

import java.util.Arrays;

public class RegisteredCredential implements CredentialRecord {
    private final byte[] credentialId;
    private final byte[] publicKeyCose;
    private long counter;
    private final byte[] userHandle;
    private Boolean backupEligible = false;
    private Boolean backedUp = false;
    private Boolean uvInitialized = false;

    public RegisteredCredential(byte[] credentialId, byte[] publicKeyCose, long counter, byte[] userHandle) {
        this.credentialId = credentialId != null ? Arrays.copyOf(credentialId, credentialId.length) : null;
        this.publicKeyCose = publicKeyCose != null ? Arrays.copyOf(publicKeyCose, publicKeyCose.length) : null;
        this.counter = counter;
        this.userHandle = userHandle != null ? Arrays.copyOf(userHandle, userHandle.length) : null;
    }

    public byte[] getCredentialId() {
        return credentialId != null ? Arrays.copyOf(credentialId, credentialId.length) : new byte[0];
    }

    public byte[] getPublicKeyCOSE() {
        return publicKeyCose != null ? Arrays.copyOf(publicKeyCose, publicKeyCose.length) : new byte[0];
    }

    public long getSignCount() {
        return counter;
    }

    public byte[] getUserHandle() {
        return userHandle != null ? Arrays.copyOf(userHandle, userHandle.length) : null;
    }

    public byte[] getPublicKeyCose() {
        return getPublicKeyCOSE();
    }

    public long getCounter() {
        return getSignCount();
    }

    @Override
    public AttestedCredentialData getAttestedCredentialData() {
        return null;
    }

    @Override
    public void setCounter(long counter) {
        this.counter = counter;
    }

    @Override
    public Boolean isBackupEligible() {
        return backupEligible;
    }

    @Override
    public void setBackupEligible(boolean backupEligible) {
        this.backupEligible = backupEligible;
    }

    @Override
    public Boolean isBackedUp() {
        return backedUp;
    }

    @Override
    public void setBackedUp(boolean backedUp) {
        this.backedUp = backedUp;
    }

    @Override
    public Boolean isUvInitialized() {
        return uvInitialized;
    }

    @Override
    public void setUvInitialized(boolean uvInitialized) {
        this.uvInitialized = uvInitialized;
    }

    @Override
    public CollectedClientData getClientData() {
        return null;
    }
}

