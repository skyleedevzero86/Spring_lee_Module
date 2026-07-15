package com.sleekydz86.loginstudy.auth.security;

import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccountRepository;
	private final LoginProtectionService loginProtectionService;

	public DatabaseUserDetailsService(
			UserAccountRepository userAccountRepository,
			LoginProtectionService loginProtectionService) {
		this.userAccountRepository = userAccountRepository;
		this.loginProtectionService = loginProtectionService;
	}

	@Override
	@Transactional(readOnly = true)
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		loginProtectionService.assertNotLocked(username);
		return userAccountRepository.findByUsername(username)
				.map(AuthUser::new)
				.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
	}
}
