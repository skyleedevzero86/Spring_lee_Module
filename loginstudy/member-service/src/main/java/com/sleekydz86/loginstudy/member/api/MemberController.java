package com.sleekydz86.loginstudy.member.api;

import com.sleekydz86.loginstudy.member.api.MemberDtos.MemberResponse;
import com.sleekydz86.loginstudy.member.api.MemberDtos.UpdateMemberRequest;
import com.sleekydz86.loginstudy.member.config.OpenApiConfig;
import com.sleekydz86.loginstudy.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Collection;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
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
@Tag(name = "Member", description = "일반 회원 API")
@SecurityRequirement(name = OpenApiConfig.BEARER_JWT)
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping("/me")
	@Operation(summary = "내 프로필 조회", description = "JWT subject 기준 회원 프로필을 반환합니다. SCOPE_member.read 필요")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(
					responseCode = "401",
					description = "인증 실패",
					content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
			@ApiResponse(
					responseCode = "404",
					description = "회원 없음",
					content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class)))
	})
	MemberResponse me(Authentication authentication) {
		return memberService.getBySubject(subject(authentication));
	}

	@GetMapping("/{id}")
	@Operation(summary = "회원 상세 조회", description = "본인 또는 관리자만 조회 가능 (IDOR 방지)")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공"),
			@ApiResponse(
					responseCode = "403",
					description = "타 회원 접근",
					content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class)))
	})
	MemberResponse getById(
			@Parameter(description = "회원 ID", required = true) @PathVariable Long id,
			Authentication authentication) {
		return memberService.getByIdForCaller(id, subject(authentication), isAdmin(authentication));
	}

	@PatchMapping("/{id}")
	@Operation(summary = "회원 수정", description = "SCOPE_member.write 및 버전(낙관적 잠금) 필요")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공"),
			@ApiResponse(
					responseCode = "409",
					description = "낙관적 잠금 충돌",
					content = @Content(mediaType = MediaType.APPLICATION_PROBLEM_JSON_VALUE,
							schema = @Schema(implementation = ProblemDetail.class)))
	})
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
