package com.sleekydz86.oidstudy.oidc.web.factory;

import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import com.sleekydz86.oidstudy.oidc.web.resp.AdminUserResponse;
import org.springframework.stereotype.Component;

@Component
public class AdminUserResponseFactory {

    public AdminUserResponse create(UserAccount user) {
        return new AdminUserResponse(
                user.getId(),
                user.getLoginId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getNickname(),
                user.getContactNumber(),
                user.getProvider(),
                user.getProviderUserId(),
                user.getStatus(),
                user.roleSnapshot(),
                user.getCreatedAt(),
                user.getTermsAgreedAt(),
                user.getApprovedAt(),
                user.getWithdrawnAt(),
                user.getWithdrawalReason(),
                user.getLastLoginAt()
        );
    }
}