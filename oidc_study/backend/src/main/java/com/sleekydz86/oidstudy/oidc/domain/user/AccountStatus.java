package com.sleekydz86.oidstudy.oidc.domain.user;

public enum AccountStatus {
    SIGNUP_REQUIRED,
    PENDING,
    ACTIVE,
    REJECTED,
    WITHDRAWN;

    public boolean isRegistrationCompleted() {
        return this != SIGNUP_REQUIRED;
    }
}