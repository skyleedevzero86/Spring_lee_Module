package com.sleekydz86.oidstudy.oidc.domain.user;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserAccount {

    private Long id;
    private String provider;
    private String providerUserId;
    private String loginId;
    private String email;
    private String displayName;
    private String nickname;
    private String contactNumber;
    private String profileImageUrl;
    private AccountStatus status;
    private LocalDateTime termsAgreedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    private LocalDateTime approvedAt;
    private Long approvedBy;
    private LocalDateTime withdrawnAt;
    private String withdrawalReason;
    private List<String> roles = new ArrayList<>();

    public static UserAccount registerSignupRequired(UserIdentity identity, UserProfile profile) {
        UserAccount account = new UserAccount();
        account.provider = identity.provider();
        account.providerUserId = identity.providerUserId();
        account.status = AccountStatus.SIGNUP_REQUIRED;
        account.roles = new ArrayList<>();
        account.syncOidcProfile(profile);
        return account;
    }

    public void syncOidcProfile(UserProfile profile) {
        this.email = profile.email();
        if (this.displayName == null || this.displayName.isBlank() || needsRegistration()) {
            this.displayName = profile.displayName();
        }
        this.nickname = profile.nickname();
        this.profileImageUrl = profile.profileImageUrl();
    }

    public void completeRegistration(String loginId, String displayName, String contactNumber, LocalDateTime termsAgreedAt) {
        this.loginId = loginId;
        this.displayName = displayName;
        this.contactNumber = contactNumber;
        this.termsAgreedAt = termsAgreedAt;
        this.status = AccountStatus.PENDING;
        this.withdrawnAt = null;
        this.withdrawalReason = null;
        this.roles = new ArrayList<>();
    }

    public void approve(Long approverId, List<String> assignedRoles) {
        this.status = AccountStatus.ACTIVE;
        this.approvedBy = approverId;
        this.approvedAt = LocalDateTime.now();
        this.withdrawnAt = null;
        this.withdrawalReason = null;
        this.roles = new ArrayList<>(assignedRoles);
    }

    public void reject(Long approverId) {
        this.status = AccountStatus.REJECTED;
        this.approvedBy = approverId;
        this.approvedAt = null;
        this.roles = new ArrayList<>();
    }

    public void withdraw(String reason) {
        this.status = AccountStatus.WITHDRAWN;
        this.withdrawnAt = LocalDateTime.now();
        this.withdrawalReason = reason;
        this.approvedAt = null;
        this.roles = new ArrayList<>();
    }

    public UserIdentity identity() {
        return new UserIdentity(provider, providerUserId);
    }

    public UserProfile profile() {
        return new UserProfile(email, displayName, nickname, profileImageUrl);
    }

    public List<String> roleSnapshot() {
        return List.copyOf(roles);
    }

    public boolean needsRegistration() {
        return status == AccountStatus.SIGNUP_REQUIRED;
    }

    public boolean isActive() {
        return status == AccountStatus.ACTIVE;
    }

    public boolean isAdmin() {
        return roles.contains(RoleCatalog.ADMIN);
    }

    public boolean isWithdrawn() {
        return status == AccountStatus.WITHDRAWN;
    }

    public boolean canWithdraw() {
        return status != AccountStatus.WITHDRAWN;
    }

    public boolean hasCompletedRegistration() {
        return status.isRegistrationCompleted() && loginId != null && !loginId.isBlank() && termsAgreedAt != null;
    }
}