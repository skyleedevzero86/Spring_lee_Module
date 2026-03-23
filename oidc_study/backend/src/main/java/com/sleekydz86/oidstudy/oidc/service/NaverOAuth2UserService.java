package com.sleekydz86.oidstudy.oidc.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.sleekydz86.oidstudy.global.auth.OidcProvisioningCommandFactory;
import com.sleekydz86.oidstudy.global.security.AppOidcUser;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.domain.user.RoleCatalog;
import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NaverOAuth2UserService extends DefaultOAuth2UserService {

    private final UserAccountApplicationService userAccountApplicationService;
    private final OidcProvisioningCommandFactory commandFactory;

    public NaverOAuth2UserService(
            UserAccountApplicationService userAccountApplicationService,
            OidcProvisioningCommandFactory commandFactory
    ) {
        this.userAccountApplicationService = userAccountApplicationService;
        this.commandFactory = commandFactory;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        Map<String, Object> normalized = normalizeNaverClaims(oauth2User.getAttributes());
        OAuth2User normalizedUser = new DefaultOAuth2User(oauth2User.getAuthorities(), normalized, "sub");

        UserAccount account = userAccountApplicationService.provision(commandFactory.create(userRequest, normalizedUser));
        Set<SimpleGrantedAuthority> authorities = resolveAuthorities(account, oauth2User);

        log.info(
                "OAuth2 login completed. provider={} accountId={} subject={} status={} roles={}",
                account.getProvider(),
                account.getId(),
                account.getProviderUserId(),
                account.getStatus(),
                account.roleSnapshot()
        );

        return new AppOidcUser(authorities, normalized, account);
    }

    private Set<SimpleGrantedAuthority> resolveAuthorities(UserAccount account, OAuth2User oauth2User) {
        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        switch (account.getStatus()) {
            case ACTIVE -> account.roleSnapshot().stream().map(SimpleGrantedAuthority::new).forEach(authorities::add);
            case PENDING -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.PENDING));
            case REJECTED -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.REJECTED));
            case WITHDRAWN -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.WITHDRAWN));
            case SIGNUP_REQUIRED -> authorities.add(new SimpleGrantedAuthority(RoleCatalog.SIGNUP_REQUIRED));
        }
        oauth2User.getAuthorities().stream()
                .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                .forEach(authorities::add);
        return authorities;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> normalizeNaverClaims(Map<String, Object> attributes) {
        Object response = attributes.get("response");
        Map<String, Object> source = response instanceof Map<?, ?> map
                ? (Map<String, Object>) map
                : attributes;

        Map<String, Object> normalized = new LinkedHashMap<>(source);
        Object id = source.get("id");
        if (id != null) {
            normalized.put("sub", String.valueOf(id));
        }
        if (source.get("name") == null && source.get("nickname") != null) {
            normalized.put("name", source.get("nickname"));
        }
        if (source.get("picture") == null && source.get("profile_image") != null) {
            normalized.put("picture", source.get("profile_image"));
        }
        return normalized;
    }
}
