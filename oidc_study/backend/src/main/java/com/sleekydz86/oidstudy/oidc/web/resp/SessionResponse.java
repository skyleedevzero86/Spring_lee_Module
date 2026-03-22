package com.sleekydz86.oidstudy.oidc.web.resp;

import java.util.Map;

public record SessionResponse(
        boolean authenticated,
        String loginUrl,
        String logoutUrl,
        SessionAccountResponse account,
        Map<String, Object> oidcClaims
) {

    public static SessionResponse anonymous() {
        return new SessionResponse(false, "/oauth2/authorization/naver", null, null, Map.of());
    }
}