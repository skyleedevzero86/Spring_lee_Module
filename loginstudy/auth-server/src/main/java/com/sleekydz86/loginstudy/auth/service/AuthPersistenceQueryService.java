package com.sleekydz86.loginstudy.auth.service;

import com.sleekydz86.loginstudy.auth.api.AuthDtos.LoginHistoryResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.PersistenceHealthResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.RegisteredClientSummaryResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.UpdateOwnProfileRequest;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.UserAccountResponse;
import com.sleekydz86.loginstudy.auth.domain.AccountStatus;
import com.sleekydz86.loginstudy.auth.domain.LoginHistory;
import com.sleekydz86.loginstudy.auth.domain.UserAccount;
import com.sleekydz86.loginstudy.auth.domain.UserRole;
import com.sleekydz86.loginstudy.auth.repository.LoginHistoryRepository;
import com.sleekydz86.loginstudy.auth.repository.UserAccountRepository;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthPersistenceQueryService {

	private final UserAccountRepository userAccountRepository;
	private final LoginHistoryRepository loginHistoryRepository;
	private final RegisteredClientRepository registeredClientRepository;
	private final OAuth2AuthorizationService authorizationService;
	private final OAuth2AuthorizationConsentService authorizationConsentService;
	private final JdbcTemplate jdbcTemplate;

	public AuthPersistenceQueryService(
			UserAccountRepository userAccountRepository,
			LoginHistoryRepository loginHistoryRepository,
			RegisteredClientRepository registeredClientRepository,
			OAuth2AuthorizationService authorizationService,
			OAuth2AuthorizationConsentService authorizationConsentService,
			JdbcTemplate jdbcTemplate) {
		this.userAccountRepository = userAccountRepository;
		this.loginHistoryRepository = loginHistoryRepository;
		this.registeredClientRepository = registeredClientRepository;
		this.authorizationService = authorizationService;
		this.authorizationConsentService = authorizationConsentService;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional(readOnly = true)
	public UserAccountResponse getUserByUsername(String username) {
		UserAccount account = userAccountRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username));
		return toUserResponse(account);
	}

	@Transactional(readOnly = true)
	public List<UserAccountResponse> findAllUsers() {
		return userAccountRepository.findAll().stream()
				.map(this::toUserResponse)
				.toList();
	}

	@Transactional
	public UserAccountResponse changeRole(String username, String role, String actorUsername) {
		UserAccount account = findAccount(username);
		String normalizedRole = role.trim().toUpperCase(Locale.ROOT);
		if (username.equals(actorUsername) && !"ADMIN".equals(normalizedRole)) {
			throw new AccessDeniedException("자신의 관리자 권한은 제거할 수 없습니다");
		}
		account.replaceRole(normalizedRole);
		return toUserResponse(userAccountRepository.saveAndFlush(account));
	}

	@Transactional
	public UserAccountResponse changeStatus(
			String username,
			AccountStatus status,
			String actorUsername) {
		UserAccount account = findAccount(username);
		if (username.equals(actorUsername) && status != AccountStatus.ACTIVE) {
			throw new AccessDeniedException("자신의 계정 상태는 제한할 수 없습니다");
		}
		account.changeStatus(status);
		return toUserResponse(userAccountRepository.saveAndFlush(account));
	}

	@Transactional(readOnly = true)
	public UserAccountResponse getOwnProfile(String username) {
		return toUserResponse(findAccount(username));
	}

	@Transactional
	public UserAccountResponse updateOwnProfile(String username, UpdateOwnProfileRequest request) {
		UserAccount account = findAccount(username);
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (userAccountRepository.existsByEmailIgnoreCaseAndIdNot(email, account.getId())) {
			throw new IllegalArgumentException("이미 사용 중인 이메일입니다");
		}
		account.changeProfile(
				request.displayName().trim(),
				email,
				request.phone().trim());
		return toUserResponse(userAccountRepository.saveAndFlush(account));
	}

	@Transactional(readOnly = true)
	public List<LoginHistoryResponse> findLoginHistory(String username, int limit) {
		int safeLimit = Math.max(1, Math.min(limit, 100));
		return loginHistoryRepository.findTop50ByUsernameOrderByCreatedAtDesc(username).stream()
				.limit(safeLimit)
				.map(this::toLoginHistoryResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public RegisteredClientSummaryResponse getRegisteredClient(String clientId) {
		RegisteredClient client = registeredClientRepository.findByClientId(clientId);
		if (client == null) {
			throw new ResourceNotFoundException("등록된 클라이언트를 찾을 수 없습니다: " + clientId);
		}
		return new RegisteredClientSummaryResponse(
				client.getId(),
				client.getClientId(),
				client.getClientName(),
				new LinkedHashSet<>(client.getAuthorizationGrantTypes().stream()
						.map(grant -> grant.getValue())
						.collect(Collectors.toCollection(LinkedHashSet::new))),
				new LinkedHashSet<>(client.getScopes()));
	}

	@Transactional(readOnly = true)
	public PersistenceHealthResponse persistenceHealth() {
		return new PersistenceHealthResponse(
				registeredClientRepository.getClass().getSimpleName(),
				authorizationService.getClass().getSimpleName(),
				authorizationConsentService.getClass().getSimpleName(),
				count("users"),
				count("oauth2_registered_client"),
				count("oauth2_authorization"),
				count("oauth2_authorization_consent"),
				count("login_history"));
	}

	public void assertJdbcBackedServices() {
		assertJdbcType("RegisteredClientRepository", registeredClientRepository);
		assertJdbcType("OAuth2AuthorizationService", authorizationService);
		assertJdbcType("OAuth2AuthorizationConsentService", authorizationConsentService);
	}

	private long count(String table) {
		Long value = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + table, Long.class);
		return value == null ? 0L : value;
	}

	private static void assertJdbcType(String name, Object bean) {
		String typeName = bean.getClass().getName();
		if (typeName.contains("InMemory")) {
			throw new IllegalStateException(name + "는 InMemory 구현체를 사용하면 안 됩니다: " + typeName);
		}
		if (!typeName.contains("Jdbc")) {
			throw new IllegalStateException(name + "는 JDBC 기반이어야 합니다: " + typeName);
		}
	}

	private UserAccountResponse toUserResponse(UserAccount account) {
		Set<String> roles = account.getRoles().stream()
				.map(UserRole::getRole)
				.collect(Collectors.toCollection(LinkedHashSet::new));
		return new UserAccountResponse(
				account.getId(),
				account.getUsername(),
				account.getEmail(),
				account.getDisplayName(),
				account.getPhone(),
				account.getTenantId(),
				account.isEnabled(),
				account.isAccountNonLocked(),
				account.getStatus(),
				roles,
				account.getCreatedAt());
	}

	private UserAccount findAccount(String username) {
		return userAccountRepository.findByUsername(username)
				.orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다: " + username));
	}

	private LoginHistoryResponse toLoginHistoryResponse(LoginHistory history) {
		return new LoginHistoryResponse(
				history.getId(),
				history.getUsername(),
				history.isSuccess(),
				history.getIpAddress(),
				history.getFailureReason(),
				history.getCreatedAt());
	}
}
