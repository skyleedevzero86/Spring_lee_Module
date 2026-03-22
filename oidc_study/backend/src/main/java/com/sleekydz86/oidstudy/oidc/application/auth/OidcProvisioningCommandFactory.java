package com.sleekydz86.oidstudy.oidc.application.auth;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sleekydz86.oidstudy.oidc.domain.UserProvisioningCommand;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class OidcProvisioningCommandFactory {

    public UserProvisioningCommand create(OidcUserRequest request, OidcUser oidcUser) {
        Map<String, Object> claims = new LinkedHashMap<>(oidcUser.getClaims());
        return new UserProvisioningCommand(
                request.getClientRegistration().getRegistrationId(),
                stringClaim(claims, StandardClaimNames.SUB),
                stringClaim(claims, StandardClaimNames.EMAIL),
                firstNonBlank(
                        stringClaim(claims, StandardClaimNames.NAME),
                        stringClaim(claims, "nickname"),
                        oidcUser.getFullName()
                ),
                firstNonBlank(stringClaim(claims, "nickname"), stringClaim(claims, StandardClaimNames.GIVEN_NAME)),
                firstNonBlank(stringClaim(claims, "profile_image"), stringClaim(claims, "picture"))
        );
    }

    private String stringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}