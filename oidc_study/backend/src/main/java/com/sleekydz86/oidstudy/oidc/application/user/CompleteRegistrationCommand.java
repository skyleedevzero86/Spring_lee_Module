package com.sleekydz86.oidstudy.oidc.application.user;

public record CompleteRegistrationCommand(
        String loginId,
        String displayName,
        String contactNumber,
        boolean agreedToTerms
) {
}