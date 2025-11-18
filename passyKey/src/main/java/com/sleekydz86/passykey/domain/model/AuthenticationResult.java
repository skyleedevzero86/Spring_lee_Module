package com.sleekydz86.passykey.domain.model;

public class AuthenticationResult {
    private String credentialId;
    private long counter;

    public AuthenticationResult() {
    }

    public AuthenticationResult(String credentialId, long counter) {
        this.credentialId = credentialId;
        this.counter = counter;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public long getCounter() {
        return counter;
    }

    public void setCounter(long counter) {
        this.counter = counter;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String credentialId;
        private long counter;

        public Builder credentialId(String credentialId) {
            this.credentialId = credentialId;
            return this;
        }

        public Builder counter(long counter) {
            this.counter = counter;
            return this;
        }

        public AuthenticationResult build() {
            return new AuthenticationResult(credentialId, counter);
        }
    }
}
