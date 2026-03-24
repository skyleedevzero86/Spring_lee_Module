package com.sleekydz86.oidstudy.oidc.domain.user;

public record UserProvisioningCommand(
        String provider,
        String providerUserId,
        String email,
        String displayName,
        String nickname,
        String profileImageUrl,
        String contactNumber
) {
}