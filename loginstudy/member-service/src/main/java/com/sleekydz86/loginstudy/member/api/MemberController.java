package com.sleekydz86.loginstudy.member.api;

import com.sleekydz86.loginstudy.member.api.MemberDtos.MemberResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.UpdateMemberRequest;
import com.sleekydz86.loginstudy.member.service.MemberService;
import jakarta.validation.Valid;
import java.util.Collection;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping("/me")
	MemberResponse me(Authentication authentication) {
		return memberService.getBySubject(subject(authentication));
	}

	@GetMapping("/{id}")
	MemberResponse getById(@PathVariable Long id, Authentication authentication) {
		return memberService.getByIdForCaller(id, subject(authentication), isAdmin(authentication));
	}

	@PatchMapping("/{id}")
	MemberResponse update(
			@PathVariable Long id,
			@Valid @RequestBody UpdateMemberRequest request,
			Authentication authentication) {
		return memberService.update(id, subject(authentication), isAdmin(authentication), request);
	}

	private static String subject(Authentication authentication) {
		if (authentication.getPrincipal() instanceof Jwt jwt) {
			return jwt.getSubject();
		}
		return authentication.getName();
	}

	private static boolean isAdmin(Authentication authentication) {
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		boolean roleAdmin = authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		boolean scopeAdmin = authorities.stream().anyMatch(a -> "SCOPE_admin".equals(a.getAuthority()));
		return roleAdmin && scopeAdmin;
	}
}
