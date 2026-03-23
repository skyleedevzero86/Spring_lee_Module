package com.sleekydz86.oidstudy.global.auth;

import java.util.LinkedHashMap;
import java.util.Map;

import com.sleekydz86.oidstudy.oidc.domain.user.UserProvisioningCommand;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class OidcProvisioningCommandFactory {

    public UserProvisioningCommand create(OAuth2UserRequest request, OAuth2User user) {
        Map<String, Object> claims = new LinkedHashMap<>(user.getAttributes());
        return create(request.getClientRegistration().getRegistrationId(), claims, user.getName());
    }

    public UserProvisioningCommand create(OidcUserRequest request, OidcUser user) {
        Map<String, Object> claims = new LinkedHashMap<>(user.getClaims());
        return create(request.getClientRegistration().getRegistrationId(), claims, user.getFullName());
    }

    private UserProvisioningCommand create(String registrationId, Map<String, Object> claims, String fallbackName) {
        return new UserProvisioningCommand(
                registrationId,
                stringClaim(claims, StandardClaimNames.SUB),
                stringClaim(claims, StandardClaimNames.EMAIL),
                firstNonBlank(
                        stringClaim(claims, StandardClaimNames.NAME),
                        stringClaim(claims, "nickname"),
                        fallbackName),
                firstNonBlank(stringClaim(claims, "nickname"), stringClaim(claims, StandardClaimNames.GIVEN_NAME)),
                firstNonBlank(stringClaim(claims, "profile_image"), stringClaim(claims, "picture")));
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