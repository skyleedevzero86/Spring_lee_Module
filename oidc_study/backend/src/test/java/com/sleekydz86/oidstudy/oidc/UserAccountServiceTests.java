package com.sleekydz86.oidstudy.oidc;

import java.util.List;

import com.sleekydz86.oidstudy.oidc.application.user.CompleteRegistrationCommand;
import com.sleekydz86.oidstudy.oidc.application.user.LoginIdCheckStatus;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.domain.user.AccountStatus;
import com.sleekydz86.oidstudy.oidc.domain.user.RoleCatalog;
import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;
import com.sleekydz86.oidstudy.oidc.domain.user.UserProvisioningCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@ActiveProfiles("test")
class UserAccountApplicationServiceTests {

    @Autowired
    private UserAccountApplicationService userAccountApplicationService;

    @Test
    void newUserShouldStartInSignupRequired() {
        UserAccount user = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "signup-subject", "member@example.com", "Pending Member", "member", null)
        );

        assertThat(user.getStatus()).isEqualTo(AccountStatus.SIGNUP_REQUIRED);
        assertThat(user.roleSnapshot()).isEmpty();
    }

    @Test
    void completingRegistrationShouldMoveUserToPending() {
        UserAccount user = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "registration-subject", "member2@example.com", "Pending Member", "member2", null)
        );

        UserAccount registered = userAccountApplicationService.completeRegistration(
                user.getId(),
                new CompleteRegistrationCommand("member001", "홍길동", "010-1234-5678", true)
        );

        assertThat(registered.getStatus()).isEqualTo(AccountStatus.PENDING);
        assertThat(registered.getLoginId()).isEqualTo("member001");
        assertThat(registered.getContactNumber()).isEqualTo("010-1234-5678");
        assertThat(registered.getTermsAgreedAt()).isNotNull();
    }

    @Test
    void bootstrapAdminShouldBecomeActiveAfterRegistration() {
        UserAccount user = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "admin-subject", "admin@example.com", "Admin User", "admin", null)
        );

        UserAccount approved = userAccountApplicationService.completeRegistration(
                user.getId(),
                new CompleteRegistrationCommand("admin001", "관리자", "010-2222-3333", true)
        );

        assertThat(approved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(approved.roleSnapshot()).contains(RoleCatalog.ADMIN, RoleCatalog.USER);
    }

    @Test
    void withdrawnLoginIdShouldRequireAdminInquiry() {
        UserAccount user = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "withdraw-subject", "withdraw@example.com", "Withdraw User", "withdraw", null)
        );
        UserAccount registered = userAccountApplicationService.completeRegistration(
                user.getId(),
                new CompleteRegistrationCommand("legacyid", "탈퇴회원", "010-4444-5555", true)
        );

        userAccountApplicationService.withdraw(registered.getId(), "테스트 탈퇴");

        assertThat(userAccountApplicationService.checkLoginId("legacyid", -1L).status())
                .isEqualTo(LoginIdCheckStatus.WITHDRAWN_MEMBER);
    }

    @Test
    void approvedUserShouldReceiveNormalizedRoles() {
        UserAccount admin = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "admin-subject-2", "admin@example.com", "Admin User", "admin", null)
        );
        UserAccount approvedAdmin = userAccountApplicationService.completeRegistration(
                admin.getId(),
                new CompleteRegistrationCommand("admin002", "관리자2", "010-1111-2222", true)
        );
        UserAccount member = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "member-subject-2", "user2@example.com", "Normal User", "user2", null)
        );
        UserAccount registeredMember = userAccountApplicationService.completeRegistration(
                member.getId(),
                new CompleteRegistrationCommand("member002", "일반회원", "010-3333-4444", true)
        );

        UserAccount approved = userAccountApplicationService.approve(registeredMember.getId(), approvedAdmin.getId(), List.of(RoleCatalog.MANAGER));

        assertThat(approved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(approved.roleSnapshot()).containsExactly(RoleCatalog.MANAGER);
    }

    @Test
    void approvalShouldRequireAtLeastOneRole() {
        UserAccount admin = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "admin-subject-3", "admin3@example.com", "Admin User", "admin3", null)
        );
        UserAccount approvedAdmin = userAccountApplicationService.completeRegistration(
                admin.getId(),
                new CompleteRegistrationCommand("admin003", "관리자3", "010-5555-6666", true)
        );
        UserAccount member = userAccountApplicationService.provision(
                new UserProvisioningCommand("naver", "member-subject-3", "user3@example.com", "Normal User", "user3", null)
        );
        UserAccount registeredMember = userAccountApplicationService.completeRegistration(
                member.getId(),
                new CompleteRegistrationCommand("member003", "일반회원3", "010-7777-8888", true)
        );

        assertThatThrownBy(() -> userAccountApplicationService.approve(registeredMember.getId(), approvedAdmin.getId(), List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("권한은 최소 1개 이상 선택해야 합니다.");
    }
}