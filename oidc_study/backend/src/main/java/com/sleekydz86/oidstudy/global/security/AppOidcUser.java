package com.sleekydz86.oidstudy.global.security;

import java.util.Collection;
import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.StandardClaimNames;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;

public class AppOidcUser extends DefaultOidcUser {

    private final UserAccount account;

    public AppOidcUser(
            Collection<? extends GrantedAuthority> authorities,
            OidcIdToken idToken,
            OidcUserInfo userInfo,
            UserAccount account
    ) {
        super(authorities, idToken, userInfo, StandardClaimNames.SUB);
        this.account = account;
    }

    public UserAccount getAccount() {
        return account;
    }
}