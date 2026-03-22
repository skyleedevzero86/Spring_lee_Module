package com.sleekydz86.oidstudy.oidc.domain;

import java.util.Objects;

public record UserIdentity(String provider, String providerUserId) {

    public UserIdentity {
        provider = normalizeRequired(provider, "provider");
        providerUserId = normalizeRequired(providerUserId, "providerUserId");
    }

    private static String normalizeRequired(String value, String field) {
        String normalized = Objects.requireNonNull(value, field + " must not be null").trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
        return normalized;
    }
}