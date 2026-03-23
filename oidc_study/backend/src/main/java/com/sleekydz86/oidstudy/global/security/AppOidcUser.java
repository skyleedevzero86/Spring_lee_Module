package com.sleekydz86.oidstudy.global.security;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

public class AppOidcUser extends DefaultOAuth2User implements OidcUser {

    private final UserAccount account;
    private final OidcIdToken idToken;
    private final OidcUserInfo userInfo;

    public AppOidcUser(
            Collection<? extends GrantedAuthority> authorities,
            Map<String, Object> attributes,
            UserAccount account) {
        super(authorities, attributes, "sub");
        this.idToken = null;
        this.userInfo = null;
        this.account = account;
    }

    public AppOidcUser(
            Collection<? extends GrantedAuthority> authorities,
            OidcIdToken idToken,
            OidcUserInfo userInfo,
            Map<String, Object> attributes,
            UserAccount account
    ) {
        super(authorities, attributes, "sub");
        this.idToken = idToken;
        this.userInfo = userInfo;
        this.account = account;
    }

    public UserAccount getAccount() {
        return account;
    }

    public Map<String, Object> getClaims() {
        return new LinkedHashMap<>(getAttributes());
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}