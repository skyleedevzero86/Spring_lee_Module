package com.sleekydz86.oidstudy.oidc.service;

import java.util.LinkedHashSet;
import java.util.Set;
import com.sleekydz86.oidstudy.global.auth.OidcProvisioningCommandFactory;
import com.sleekydz86.oidstudy.global.security.AppOidcUser;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.domain.RoleCatalog;
import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NaverOidcUserService extends OidcUserService {

    private final UserAccountApplicationService userAccountApplicationService;
    private final OidcProvisioningCommandFactory commandFactory;

    public NaverOidcUserService(
            UserAccountApplicationService userAccountApplicationService,
            OidcProvisioningCommandFactory commandFactory
    ) {
        this.userAccountApplicationService = userAccountApplicationService;
        this.commandFactory = commandFactory;
    }

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);
        UserAccount account = userAccountApplicationService.provision(commandFactory.create(userRequest, oidcUser));
        Set<SimpleGrantedAuthority> authorities = resolveAuthorities(account, oidcUser);

        log.info(
                "OIDC login completed. provider={} accountId={} subject={} status={} roles={}",
                account.getProvider(),
                account.getId(),
                account.getProviderUserId(),
                account.getStatus(),
                account.roleSnapshot()
        );

        return new AppOidcUser(authorities, oidcUser.getIdToken(), oidcUser.getUserInfo(), account);
    }

    private Set<SimpleGrantedAuthority> resolveAuthorities(UserAccount account, OidcUser oidcUser) {
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        switch (account.getStatus()) {
            case ACTIVE -> account.roleSnapshot().stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
            case PENDING -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.PENDING));
            case REJECTED -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.REJECTED));
            case WITHDRAWN -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.WITHDRAWN));
            case SIGNUP_REQUIRED -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.SIGNUP_REQUIRED));
        }
        oidcUser.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .forEach(authorities::add);
        return authorities;
    }
}