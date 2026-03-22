package com.sleekydz86.oidstudy.oidc.web.factory;

import java.util.LinkedHashMap;
import com.sleekydz86.oidstudy.global.security.AppOidcUser;
import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import com.sleekydz86.oidstudy.oidc.web.resp.SessionAccountResponse;
import com.sleekydz86.oidstudy.oidc.web.resp.SessionResponse;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component
public class SessionResponseFactory {

    public SessionResponse anonymous() {
        return SessionResponse.anonymous();
    }

    public SessionResponse create(AppOidcUser principal, UserAccount account) {
        return new SessionResponse(
                true,
                "/oauth2/authorization/naver",
                "/logout",
                new SessionAccountResponse(
                        account.getId(),
                        account.getProvider(),
                        account.getProviderUserId(),
                        account.getLoginId(),
                        account.getEmail(),
                        account.getDisplayName(),
                        account.getNickname(),
                        account.getContactNumber(),
                        account.getProfileImageUrl(),
                        account.getStatus(),
                        account.roleSnapshot(),
                        account.isActive(),
                        account.isAdmin(),
                        account.needsRegistration(),
                        account.isWithdrawn(),
                        account.canWithdraw(),
                        account.getTermsAgreedAt(),
                        account.getApprovedAt(),
                        account.getWithdrawnAt(),
                        account.getLastLoginAt(),
                        account.getCreatedAt()
                ),
                new LinkedHashMap<>(principal.getClaims())
        );
    }

    public SessionResponse create(OidcUser principal) {
        return new SessionResponse(
                true,
                "/oauth2/authorization/naver",
                "/logout",
                null,
                new LinkedHashMap<>(principal.getClaims())
        );
    }
}