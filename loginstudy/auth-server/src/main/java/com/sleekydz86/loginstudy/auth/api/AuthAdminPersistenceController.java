package com.sleekydz86.loginstudy.auth.api;

import com.sleekydz86.loginstudy.auth.api.AuthDtos.LoginHistoryResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.PersistenceHealthResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.RegisteredClientSummaryResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.UserAccountResponse;
import com.sleekydz86.loginstudy.auth.service.AuthPersistenceQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AuthAdminPersistenceController {

	private final AuthPersistenceQueryService authPersistenceQueryService;

	public AuthAdminPersistenceController(AuthPersistenceQueryService authPersistenceQueryService) {
		this.authPersistenceQueryService = authPersistenceQueryService;
	}

	@GetMapping("/persistence/health")
	PersistenceHealthResponse persistenceHealth() {
		authPersistenceQueryService.assertJdbcBackedServices();
		return authPersistenceQueryService.persistenceHealth();
	}

	@GetMapping("/users/{username}")
	UserAccountResponse getUser(@PathVariable String username) {
		return authPersistenceQueryService.getUserByUsername(username);
	}

	@GetMapping("/login-history")
	List<LoginHistoryResponse> loginHistory(
			@RequestParam String username,
			@RequestParam(defaultValue = "20") int limit) {
		return authPersistenceQueryService.findLoginHistory(username, limit);
	}

	@GetMapping("/clients/{clientId}")
	RegisteredClientSummaryResponse getClient(@PathVariable String clientId) {
		return authPersistenceQueryService.getRegisteredClient(clientId);
	}
}
