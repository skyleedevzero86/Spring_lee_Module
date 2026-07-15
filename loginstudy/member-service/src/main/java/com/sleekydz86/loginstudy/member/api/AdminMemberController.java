package com.sleekydz86.loginstudy.member.api;

import com.sleekydz86.loginstudy.member.api.MemberDtos.ChangeStatusRequest;
import com.sleekydz86.loginstudy.member.api.MemberDtos.KeysetPageResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.MemberResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.MemberSummaryResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.PageResponse;
import com.sleekydz86.loginstudy.member.config.OpenApiConfig;
import com.sleekydz86.loginstudy.member.domain.MemberStatus;
import com.sleekydz86.loginstudy.member.service.AccessDeniedBusinessException;
import com.sleekydz86.loginstudy.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Collection;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/members")
@PreAuthorize("hasAuthority('SCOPE_admin') and hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin Member", description = "관리자 회원 API (ROLE_ADMIN + SCOPE_admin)")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class AdminMemberController {

	private final MemberService memberService;

	public AdminMemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	@Operation(summary = "회원 OFFSET 검색", description = "상태/이메일/이름/가입일 범위 + page/size")
	PageResponse<MemberSummaryResponse> search(
			@RequestParam(required = false) MemberStatus status,
			@RequestParam(required = false) String email,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant joinedFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant joinedTo,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size,
			Authentication authentication) {
		assertAdmin(authentication);
		return memberService.search(
				status,
				email,
				name,
				joinedFrom,
				joinedTo,
				PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "joinedAt")));
	}

	@GetMapping("/keyset")
	@Operation(summary = "회원 Keyset 검색", description = "깊은 페이지 OFFSET 대안. cursorJoinedAt + cursorId")
	KeysetPageResponse searchKeyset(
			@RequestParam(required = false) MemberStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant joinedFrom,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant joinedTo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant cursorJoinedAt,
			@RequestParam(required = false) Long cursorId,
			@RequestParam(defaultValue = "20") int size,
			Authentication authentication) {
		assertAdmin(authentication);
		return memberService.searchKeyset(status, joinedFrom, joinedTo, cursorJoinedAt, cursorId, size);
	}

	@GetMapping("/{id}")
	@Operation(summary = "관리자 회원 상세")
	MemberResponse get(
			@Parameter(required = true) @PathVariable Long id,
			Authentication authentication) {
		assertAdmin(authentication);
		return memberService.getByIdForCaller(id, subject(authentication), true);
	}

	@PostMapping("/{id}/status")
	@Operation(summary = "회원 상태 변경")
	MemberResponse changeStatus(
			@PathVariable Long id,
			@Valid @RequestBody ChangeStatusRequest request,
			Authentication authentication) {
		assertAdmin(authentication);
		return memberService.changeStatus(id, subject(authentication), request);
	}

	private static void assertAdmin(Authentication authentication) {
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		boolean roleAdmin = authorities.stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
		boolean scopeAdmin = authorities.stream().anyMatch(a -> "SCOPE_admin".equals(a.getAuthority()));
		if (!(roleAdmin && scopeAdmin)) {
			throw new AccessDeniedBusinessException("ROLE_ADMIN과 SCOPE_admin 권한이 모두 필요합니다");
		}
	}

	private static String subject(Authentication authentication) {
		if (authentication.getPrincipal() instanceof Jwt jwt) {
			return jwt.getSubject();
		}
		return authentication.getName();
	}
}
