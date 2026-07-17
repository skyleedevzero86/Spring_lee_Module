package com.sleekydz86.loginstudy.auth.api;

import com.sleekydz86.loginstudy.auth.api.AuthDtos.ChangeAccountStatusRequest;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.ChangeRoleRequest;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.LoginHistoryResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.PersistenceHealthResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.RegisteredClientSummaryResponse;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.UserAccountResponse;
import com.sleekydz86.loginstudy.auth.config.OpenApiConfig;
import com.sleekydz86.loginstudy.auth.service.AuthPersistenceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Auth Admin", description = "영속 검증/조회 Admin API (ROLE_ADMIN + 세션)")
@SecurityRequirement(name = OpenApiConfig.SESSION_COOKIE)
public class AuthAdminPersistenceController {

	private final AuthPersistenceQueryService authPersistenceQueryService;

	public AuthAdminPersistenceController(AuthPersistenceQueryService authPersistenceQueryService) {
		this.authPersistenceQueryService = authPersistenceQueryService;
	}

	@GetMapping("/persistence/health")
	@Operation(summary = "JDBC 영속 헬스", description = "InMemory 저장소 사용 여부를 확인하고 카운트를 반환합니다.")
	PersistenceHealthResponse persistenceHealth() {
		authPersistenceQueryService.assertJdbcBackedServices();
		return authPersistenceQueryService.persistenceHealth();
	}

	@GetMapping("/users/{username}")
	@Operation(summary = "사용자 조회", description = "비밀번호는 응답에 포함되지 않습니다.")
	UserAccountResponse getUser(@PathVariable String username) {
		return authPersistenceQueryService.getUserByUsername(username);
	}

	@GetMapping("/users")
	@Operation(summary = "전체 사용자 조회")
	List<UserAccountResponse> users() {
		return authPersistenceQueryService.findAllUsers();
	}

	@PostMapping("/users/{username}/role")
	@Operation(summary = "사용자 권한 변경")
	UserAccountResponse changeRole(
			@PathVariable String username,
			@Valid @RequestBody ChangeRoleRequest request,
			Authentication authentication) {
		return authPersistenceQueryService.changeRole(username, request.role(), authentication.getName());
	}

	@PostMapping("/users/{username}/status")
	@Operation(summary = "사용자 계정 상태 변경")
	UserAccountResponse changeStatus(
			@PathVariable String username,
			@Valid @RequestBody ChangeAccountStatusRequest request,
			Authentication authentication) {
		return authPersistenceQueryService.changeStatus(username, request.status(), authentication.getName());
	}

	@GetMapping("/login-history")
	@Operation(summary = "로그인 이력 조회")
	List<LoginHistoryResponse> loginHistory(
			@RequestParam String username,
			@RequestParam(defaultValue = "20") int limit) {
		return authPersistenceQueryService.findLoginHistory(username, limit);
	}

	@GetMapping("/clients/{clientId}")
	@Operation(summary = "등록 클라이언트 조회")
	RegisteredClientSummaryResponse getClient(@PathVariable String clientId) {
		return authPersistenceQueryService.getRegisteredClient(clientId);
	}
}
