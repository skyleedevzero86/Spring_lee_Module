package com.sleekydz86.loginstudy.auth.api;

import com.sleekydz86.loginstudy.auth.api.AuthDtos.UpdateOwnProfileRequest;
import com.sleekydz86.loginstudy.auth.api.AuthDtos.UserAccountResponse;
import com.sleekydz86.loginstudy.auth.service.AuthPersistenceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@Tag(name = "Auth Account", description = "로그인 사용자 개인정보 API")
public class AuthAccountController {

	private final AuthPersistenceQueryService authPersistenceQueryService;

	public AuthAccountController(AuthPersistenceQueryService authPersistenceQueryService) {
		this.authPersistenceQueryService = authPersistenceQueryService;
	}

	@GetMapping("/me")
	@Operation(summary = "내 개인정보 조회")
	UserAccountResponse me(Authentication authentication) {
		return authPersistenceQueryService.getOwnProfile(authentication.getName());
	}

	@PatchMapping("/me")
	@Operation(summary = "내 개인정보 수정")
	UserAccountResponse update(
			@Valid @RequestBody UpdateOwnProfileRequest request,
			Authentication authentication) {
		return authPersistenceQueryService.updateOwnProfile(authentication.getName(), request);
	}
}
