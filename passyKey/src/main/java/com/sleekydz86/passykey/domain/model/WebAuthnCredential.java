package com.sleekydz86.passykey.domain.model;

import java.time.LocalDateTime;

public class WebAuthnCredential {

    private Long id;
    private String credentialId;
    private String publicKeyCose;
    private Long counter;
    private String transports;
    private String label;
    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;

    public WebAuthnCredential() {
    }

    public WebAuthnCredential(String credentialId, String publicKeyCose, Long counter, String transports, User user) {
        this.credentialId = credentialId;
        this.publicKeyCose = publicKeyCose;
        this.counter = counter;
        this.transports = transports;
        this.user = user;
        this.createdAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
    }

    public void updateCounter(Long newCounter) {
        if (newCounter <= this.counter) {
            throw new IllegalArgumentException("카운터는 현재 값보다 커야 합니다");
        }
        this.counter = newCounter;
        updateLastUsed();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    public String getPublicKeyCose() {
        return publicKeyCose;
    }

    public void setPublicKeyCose(String publicKeyCose) {
        this.publicKeyCose = publicKeyCose;
    }

    public Long getCounter() {
        return counter;
    }

    public void setCounter(Long counter) {
        this.counter = counter;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
