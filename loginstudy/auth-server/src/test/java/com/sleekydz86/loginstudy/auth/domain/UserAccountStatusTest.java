package com.sleekydz86.loginstudy.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserAccountStatusTest {

	@Test
	@DisplayName("정지 계정은 잠기지만 활성 상태로 되돌릴 수 있다")
	void suspendedAccountCanBeReactivated() {
		// given
		UserAccount account = account();

		// when
		account.changeStatus(AccountStatus.SUSPENDED);

		// then
		assertThat(account.isEnabled()).isTrue();
		assertThat(account.isAccountNonLocked()).isFalse();

		// when
		account.changeStatus(AccountStatus.ACTIVE);

		// then
		assertThat(account.isEnabled()).isTrue();
		assertThat(account.isAccountNonLocked()).isTrue();
	}

	@Test
	@DisplayName("탈퇴 계정은 로그인이 비활성화되지만 관리자가 복구할 수 있다")
	void withdrawnAccountCanBeReactivated() {
		// given
		UserAccount account = account();

		// when
		account.changeStatus(AccountStatus.WITHDRAWN);

		// then
		assertThat(account.isEnabled()).isFalse();

		// when
		account.changeStatus(AccountStatus.ACTIVE);

		// then
		assertThat(account.isEnabled()).isTrue();
	}

	@Test
	@DisplayName("삭제 계정은 어떤 상태로도 복구할 수 없다")
	void deletedAccountCannotBeReactivated() {
		// given
		UserAccount account = account();
		account.changeStatus(AccountStatus.DELETED);

		// when / then
		assertThatIllegalArgumentException()
				.isThrownBy(() -> account.changeStatus(AccountStatus.ACTIVE))
				.withMessageContaining("복구할 수 없습니다");
	}

	@Test
	@DisplayName("계정 권한은 사용자와 관리자 중 하나로 교체한다")
	void roleCanBeReplaced() {
		// given
		UserAccount account = account();

		// when
		account.replaceRole("ADMIN");

		// then
		assertThat(account.getRoles())
				.extracting(UserRole::getRole)
				.containsExactly("ADMIN");
	}

	private static UserAccount account() {
		UserAccount account = new UserAccount(
				"user",
				"{bcrypt}password",
				"user@example.com",
				"Demo User",
				"010-1111-2222",
				"PERSONAL",
				"tenant-demo",
				Instant.now());
		account.addRole("USER");
		return account;
	}
}
