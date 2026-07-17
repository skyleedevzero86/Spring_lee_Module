package com.sleekydz86.loginstudy.auth.service;

import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import com.sleekydz86.loginstudy.auth.web.RegistrationForm;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final String tenantId;

	public RegistrationService(
			UserAccountRepository userAccountRepository,
			PasswordEncoder passwordEncoder,
			@Value("${auth.bootstrap.tenant-id:tenant-demo}") String tenantId) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.tenantId = tenantId;
	}

	@Transactional
	public void register(RegistrationForm form) {
		String username = form.getUsername().trim().toLowerCase(Locale.ROOT);
		String email = form.getEmail().trim().toLowerCase(Locale.ROOT);

		if (userAccountRepository.existsByUsername(username)) {
			throw new DuplicateRegistrationException("username");
		}
		if (userAccountRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateRegistrationException("email");
		}

		Instant acceptedAt = Instant.now();
		UserAccount account = new UserAccount(
				username,
				passwordEncoder.encode(form.getPassword()),
				email,
				tenantId,
				form.getDisplayName().trim(),
				form.getPhone().trim(),
				form.getMemberType(),
				acceptedAt);
		account.addRole("USER");

		try {
			userAccountRepository.saveAndFlush(account);
		}
		catch (DataIntegrityViolationException ex) {
			throw new DuplicateRegistrationException("usernameOrEmail");
		}
	}

	public static final class DuplicateRegistrationException extends RuntimeException {

		private final String field;

		public DuplicateRegistrationException(String field) {
			super("Duplicate registration field: " + field);
			this.field = field;
		}

		public String getField() {
			return field;
		}
	}
}
