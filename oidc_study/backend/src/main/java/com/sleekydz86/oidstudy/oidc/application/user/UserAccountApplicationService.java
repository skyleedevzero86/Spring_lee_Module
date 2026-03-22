package com.sleekydz86.oidstudy.oidc.application.user;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import com.sleekydz86.oidstudy.oidc.domain.*;
import com.sleekydz86.oidstudy.oidc.domain.policy.BootstrapAdministratorPolicy;
import com.sleekydz86.oidstudy.oidc.domain.policy.RoleAssignmentPolicy;
import com.sleekydz86.oidstudy.oidc.domain.repository.RoleCatalogRepository;
import com.sleekydz86.oidstudy.oidc.domain.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserAccountApplicationService {

    private static final Pattern LOGIN_ID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_-]{3,19}$");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^[0-9-]{8,20}$");

    private final UserAccountRepository userAccountRepository;
    private final RoleCatalogRepository roleCatalogRepository;
    private final RoleAssignmentPolicy roleAssignmentPolicy;
    private final BootstrapAdministratorPolicy bootstrapAdministratorPolicy;
    private final AdminNotificationRepository adminNotificationRepository;

    public UserAccountApplicationService(
            UserAccountRepository userAccountRepository,
            RoleCatalogRepository roleCatalogRepository,
            RoleAssignmentPolicy roleAssignmentPolicy,
            BootstrapAdministratorPolicy bootstrapAdministratorPolicy,
            AdminNotificationRepository adminNotificationRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.roleCatalogRepository = roleCatalogRepository;
        this.roleAssignmentPolicy = roleAssignmentPolicy;
        this.bootstrapAdministratorPolicy = bootstrapAdministratorPolicy;
        this.adminNotificationRepository = adminNotificationRepository;
    }

    @Transactional
    public UserAccount provision(UserProvisioningCommand command) {
        roleCatalogRepository.ensureDefaultCatalog();
        UserIdentity identity = new UserIdentity(command.provider(), command.providerUserId());
        UserProfile profile = new UserProfile(command.email(), command.displayName(), command.nickname(), command.profileImageUrl());

        UserAccount account = userAccountRepository.findByIdentity(identity)
                .map(existing -> refreshExistingAccount(existing, profile))
                .orElseGet(() -> userAccountRepository.save(UserAccount.registerSignupRequired(identity, profile)));

        UserAccount hydrated = getRequired(account.getId());
        if (hydrated.getStatus() == AccountStatus.PENDING && bootstrapAdministratorPolicy.shouldBootstrap(hydrated)) {
            return approve(hydrated.getId(), hydrated.getId(), List.of(RoleCatalog.ADMIN));
        }
        return hydrated;
    }

    @Transactional(readOnly = true)
    public UserAccount getRequired(Long userId) {
        UserAccount account = userAccountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        account.setRoles(roleCatalogRepository.findRolesByUserId(userId));
        return account;
    }

    @Transactional(readOnly = true)
    public List<UserAccount> findAll(AccountStatus status) {
        return userAccountRepository.findAll(status).stream()
                .map(account -> {
                    account.setRoles(roleCatalogRepository.findRolesByUserId(account.getId()));
                    return account;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public LoginIdCheckResult checkLoginId(String rawLoginId, Long currentUserId) {
        try {
            String loginId = normalizeLoginId(rawLoginId);
            UserAccount duplicate = userAccountRepository.findByLoginId(loginId).orElse(null);
            if (duplicate == null) {
                return new LoginIdCheckResult(true, LoginIdCheckStatus.AVAILABLE, "사용 가능한 아이디입니다.");
            }
            if (duplicate.getId().equals(currentUserId) && duplicate.getStatus() != AccountStatus.WITHDRAWN) {
                return new LoginIdCheckResult(true, LoginIdCheckStatus.AVAILABLE_CURRENT_USER, "현재 계정에서 사용할 수 있는 아이디입니다.");
            }
            if (duplicate.getStatus() == AccountStatus.WITHDRAWN) {
                return new LoginIdCheckResult(false, LoginIdCheckStatus.WITHDRAWN_MEMBER, "탈퇴 이력이 있는 아이디입니다. 관리자에게 문의하세요.");
            }
            return new LoginIdCheckResult(false, LoginIdCheckStatus.EXISTING_MEMBER, "이미 가입된 계정입니다.");
        } catch (IllegalArgumentException ex) {
            return new LoginIdCheckResult(false, LoginIdCheckStatus.INVALID, ex.getMessage());
        }
    }

    @Transactional
    public UserAccount completeRegistration(Long userId, CompleteRegistrationCommand command) {
        if (!command.agreedToTerms()) {
            throw new IllegalArgumentException("약관 동의가 필요합니다.");
        }

        String loginId = normalizeLoginId(command.loginId());
        String displayName = normalizeDisplayName(command.displayName());
        String contactNumber = normalizeContactNumber(command.contactNumber());
        UserAccount duplicate = userAccountRepository.findByLoginId(loginId).orElse(null);
        if (duplicate != null && !duplicate.getId().equals(userId)) {
            throw duplicateError(duplicate);
        }

        UserAccount account = getRequired(userId);
        if (account.isWithdrawn()) {
            throw new IllegalStateException("탈퇴한 회원입니다. 관리자에게 문의하세요.");
        }
        if (!account.needsRegistration()) {
            throw new IllegalStateException("이미 가입 절차가 완료된 계정입니다.");
        }

        account.completeRegistration(loginId, displayName, contactNumber, LocalDateTime.now());
        userAccountRepository.updateRegistration(account);
        UserAccount hydrated = getRequired(userId);
        if (bootstrapAdministratorPolicy.shouldBootstrap(hydrated)) {
            return approve(hydrated.getId(), hydrated.getId(), List.of(RoleCatalog.ADMIN));
        }
        return hydrated;
    }

    @Transactional
    public UserAccount approve(Long targetUserId, Long approverUserId, List<String> requestedRoles) {
        roleCatalogRepository.ensureDefaultCatalog();
        UserAccount account = getRequired(targetUserId);
        List<String> normalizedRoles = roleAssignmentPolicy.normalize(requestedRoles);
        roleCatalogRepository.replaceUserRoles(targetUserId, normalizedRoles);
        account.approve(approverUserId, normalizedRoles);
        userAccountRepository.updateStatus(account);
        return getRequired(targetUserId);
    }

    @Transactional
    public UserAccount reject(Long targetUserId, Long approverUserId) {
        roleCatalogRepository.ensureDefaultCatalog();
        UserAccount account = getRequired(targetUserId);
        roleCatalogRepository.replaceUserRoles(targetUserId, List.of());
        account.reject(approverUserId);
        userAccountRepository.updateStatus(account);
        return getRequired(targetUserId);
    }

    @Transactional
    public UserAccount withdraw(Long userId, String reason) {
        roleCatalogRepository.ensureDefaultCatalog();
        UserAccount account = getRequired(userId);
        if (account.isWithdrawn()) {
            return account;
        }
        roleCatalogRepository.replaceUserRoles(userId, List.of());
        account.withdraw(normalizeReason(reason));
        userAccountRepository.updateStatus(account);
        adminNotificationRepository.save(AdminNotification.withdrawal(account));
        return getRequired(userId);
    }

    @Transactional(readOnly = true)
    public List<AdminNotification> findRecentNotifications(int limit) {
        return adminNotificationRepository.findRecent(Math.max(1, Math.min(limit, 50)));
    }

    @Transactional(readOnly = true)
    public DashboardSnapshot buildDashboard(UserAccount currentUser) {
        return new DashboardSnapshot(
                userAccountRepository.countAll(),
                userAccountRepository.countByStatus(AccountStatus.ACTIVE),
                userAccountRepository.countByStatus(AccountStatus.PENDING),
                userAccountRepository.countByStatus(AccountStatus.REJECTED),
                userAccountRepository.countByStatus(AccountStatus.WITHDRAWN),
                currentUser
        );
    }

    private UserAccount refreshExistingAccount(UserAccount existing, UserProfile profile) {
        existing.syncOidcProfile(profile);
        return userAccountRepository.updateLogin(existing);
    }

    private IllegalStateException duplicateError(UserAccount duplicate) {
        if (duplicate.getStatus() == AccountStatus.WITHDRAWN) {
            return new IllegalStateException("탈퇴 이력이 있는 아이디입니다. 관리자에게 문의하세요.");
        }
        return new IllegalStateException("이미 가입된 계정입니다.");
    }

    private String normalizeLoginId(String rawLoginId) {
        String loginId = rawLoginId == null ? "" : rawLoginId.trim().toLowerCase(Locale.ROOT);
        if (!LOGIN_ID_PATTERN.matcher(loginId).matches()) {
            throw new IllegalArgumentException("아이디는 영문 소문자, 숫자, _, - 조합 4자 이상 20자 이하로 입력하세요.");
        }
        return loginId;
    }

    private String normalizeDisplayName(String rawDisplayName) {
        String displayName = rawDisplayName == null ? "" : rawDisplayName.trim();
        if (displayName.length() < 2 || displayName.length() > 100) {
            throw new IllegalArgumentException("이름은 2자 이상 100자 이하로 입력하세요.");
        }
        return displayName;
    }

    private String normalizeContactNumber(String rawContactNumber) {
        String contactNumber = rawContactNumber == null ? "" : rawContactNumber.replaceAll("\\s+", "").trim();
        if (!CONTACT_PATTERN.matcher(contactNumber).matches()) {
            throw new IllegalArgumentException("연락처는 숫자와 하이픈만 사용해 8자 이상 20자 이하로 입력하세요.");
        }
        return contactNumber;
    }

    private String normalizeReason(String rawReason) {
        String reason = rawReason == null ? "" : rawReason.trim();
        return reason.isBlank() ? "회원 요청" : reason;
    }
}