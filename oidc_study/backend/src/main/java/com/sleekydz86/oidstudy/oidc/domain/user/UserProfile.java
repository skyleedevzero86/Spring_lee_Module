package com.sleekydz86.oidstudy.oidc.domain.user;

public record UserProfile(
        String email,
        String displayName,
        String nickname,
        String profileImageUrl,
        String contactNumber
) {

    public UserProfile {
        email = normalize(email);
        displayName = normalize(displayName);
        nickname = normalize(nickname);
        profileImageUrl = normalize(profileImageUrl);
        contactNumber = normalize(contactNumber);
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}