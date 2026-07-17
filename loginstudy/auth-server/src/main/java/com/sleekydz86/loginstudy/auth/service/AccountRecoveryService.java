package com.sleekydz86.loginstudy.auth.service;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountRecoveryService {

	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;

	public AccountRecoveryService(
			UserAccountRepository userAccountRepository,
			PasswordEncoder passwordEncoder) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<String> findUsernames(String displayName, String phone) {
		return userAccountRepository
				.findAllByDisplayNameAndPhoneOrderByCreatedAtAsc(displayName.trim(), phone.trim())
				.stream()
				.map(UserAccount::getUsername)
				.toList();
	}

	@Transactional(readOnly = true)
	public boolean verifyPasswordResetIdentity(String username, String email, String phone) {
		return userAccountRepository.findByUsernameAndEmailIgnoreCaseAndPhone(
				username.trim().toLowerCase(Locale.ROOT),
				email.trim().toLowerCase(Locale.ROOT),
				phone.trim()).isPresent();
	}

	@Transactional
	public void resetPassword(String username, String rawPassword) {
		UserAccount account = userAccountRepository
				.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException("Password reset account no longer exists"));
		account.changePassword(passwordEncoder.encode(rawPassword));
	}
}
