package com.sleekydz86.searchai.gateway.auth.adapter.in.web;

import com.sleekydz86.searchai.gateway.global.security.AuthUser;
import com.sleekydz86.searchai.gateway.global.security.JwtTokenService;
import com.sleekydz86.searchai.gateway.global.security.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
public final class AuthController {

	private final ReactiveUserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;

	public AuthController(
		ReactiveUserDetailsService userDetailsService,
		PasswordEncoder passwordEncoder,
		JwtTokenService jwtTokenService
	) {
		this.userDetailsService = userDetailsService;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
	}

	@PostMapping("/login")
	public Mono<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return userDetailsService.findByUsername(request.username())
			.filter(details -> passwordEncoder.matches(request.password(), details.getPassword()))
			.map(details -> {
				UserRole role = details.getAuthorities().stream()
					.findFirst()
					.map(authority -> UserRole.fromAuthority(authority.getAuthority()))
					.orElse(UserRole.USER);
				AuthUser user = new AuthUser(details.getUsername(), role);
				return new LoginResponse(jwtTokenService.issue(user), user.username(), user.role().name());
			})
			.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다")));
	}

	@GetMapping("/me")
	public Mono<MeResponse> me(Authentication authentication) {
		AuthUser user = (AuthUser) authentication.getPrincipal();
		return Mono.just(new MeResponse(user.username(), user.role().name()));
	}

	public record LoginRequest(@NotBlank String username, @NotBlank String password) {
	}

	public record LoginResponse(String token, String username, String role) {
	}

	public record MeResponse(String username, String role) {
	}
}
