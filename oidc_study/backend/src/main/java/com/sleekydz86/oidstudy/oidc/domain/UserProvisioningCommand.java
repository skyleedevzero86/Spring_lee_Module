package com.sleekydz86.oidstudy.oidc.domain;

public record UserProvisioningCommand(
        String provider,
        String providerUserId,
        String email,
        String displayName,
        String nickname,
        String profileImageUrl
) {
}