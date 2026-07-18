package com.sleekydz86.loginstudy.adminportal.web;

import com.sleekydz86.loginstudy.adminportal.service.AuthAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient.ApiCallResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import tools.jackson.databind.ObjectMapper;

@Controller
@Tag(name = "Admin Portal Pages", description = "관리자 OIDC 포털 화면")
public class AdminHomeController {

	private static final Logger log = LoggerFactory.getLogger(AdminHomeController.class);

	private final MemberAdminApiClient memberAdminApiClient;
	private final AuthAdminApiClient authAdminApiClient;
	private final ObjectMapper objectMapper;

	public AdminHomeController(
			MemberAdminApiClient memberAdminApiClient,
			AuthAdminApiClient authAdminApiClient,
			ObjectMapper objectMapper) {
		this.memberAdminApiClient = memberAdminApiClient;
		this.authAdminApiClient = authAdminApiClient;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/admin")
	@Operation(summary = "관리자 홈", description = "ROLE_ADMIN 필요. Access Token으로 member-service 관리자 API 호출")
	public String adminHome(
			@AuthenticationPrincipal OidcUser oidcUser,
			@RegisteredOAuth2AuthorizedClient("admin-portal") OAuth2AuthorizedClient authorizedClient,
			Model model) {

		String accessToken = authorizedClient.getAccessToken().getTokenValue();
		ApiCallResult apiResult = memberAdminApiClient.listMembers(accessToken);
		AuthAdminApiClient.ApiCallResult authUsersResult = authAdminApiClient.listUsers(accessToken);
		AuthAdminApiClient.ApiCallResult ownProfileResult =
				authAdminApiClient.getOwnProfile(accessToken);
		Map<String, AdminMemberView.AuthUser> accounts = parseAuthUsers(authUsersResult);
		AdminMemberView.Page members = enrichMembers(parseMembers(apiResult), accounts);

		model.addAttribute("subject", oidcUser.getSubject());
		model.addAttribute("email", oidcUser.getEmail());
		model.addAttribute("roles", oidcUser.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.sorted()
				.collect(Collectors.joining(", ")));
		model.addAttribute("scopes", authorizedClient.getAccessToken().getScopes());
		model.addAttribute("accessTokenMasked", maskToken(accessToken));
		model.addAttribute("idTokenMasked", maskToken(oidcUser.getIdToken().getTokenValue()));
		model.addAttribute("memberApiSuccess", apiResult.success());
		model.addAttribute("memberApiStatus", apiResult.statusCode());
		model.addAttribute("members", members);
		model.addAttribute("accountApiSuccess", authUsersResult.success());
		AdminMemberView.AuthUser ownProfile = parseAuthUser(ownProfileResult);
		model.addAttribute("ownProfile", ownProfile == null
				? findOwnProfile(accounts, oidcUser)
				: ownProfile);
		return "admin";
	}

	@PostMapping("/admin/members/{memberId}/status")
	public String changeStatus(
			@PathVariable long memberId,
			@RequestParam String username,
			@RequestParam String status,
			@RegisteredOAuth2AuthorizedClient("admin-portal") OAuth2AuthorizedClient authorizedClient) {
		String normalizedStatus = normalizeStatus(status);
		if (normalizedStatus == null) {
			return "redirect:/admin?accountFailed=1";
		}
		String accessToken = authorizedClient.getAccessToken().getTokenValue();
		AdminMemberView.Member member = findMember(accessToken, memberId, username);
		if (member == null || "DELETED".equals(member.accountStatus())) {
			return "redirect:/admin?accountFailed=1";
		}
		AuthAdminApiClient.ApiCallResult authResult = authAdminApiClient.changeStatus(accessToken, username,
				normalizedStatus);
		if (!authResult.success()) {
			return "redirect:/admin?accountFailed=1";
		}
		ApiCallResult memberResult = memberAdminApiClient.changeStatus(
				accessToken,
				memberId,
				normalizedStatus);
		return memberResult.success()
				? "redirect:/admin?accountUpdated=1"
				: "redirect:/admin?syncFailed=1";
	}

	@PostMapping("/admin/members/{memberId}/role")
	public String changeRole(
			@PathVariable long memberId,
			@RequestParam String username,
			@RequestParam String role,
			@RegisteredOAuth2AuthorizedClient("admin-portal") OAuth2AuthorizedClient authorizedClient) {
		String normalizedRole = switch (role) {
			case "USER" -> "USER";
			case "ADMIN" -> "ADMIN";
			default -> null;
		};
		String accessToken = authorizedClient.getAccessToken().getTokenValue();
		if (normalizedRole == null || findMember(accessToken, memberId, username) == null) {
			return "redirect:/admin?accountFailed=1";
		}
		AuthAdminApiClient.ApiCallResult result = authAdminApiClient.changeRole(accessToken, username, normalizedRole);
		return result.success()
				? "redirect:/admin?accountUpdated=1"
				: "redirect:/admin?accountFailed=1";
	}

	@PostMapping("/admin/profile")
	public String updateOwnProfile(
			@AuthenticationPrincipal OidcUser oidcUser,
			@RequestParam String displayName,
			@RequestParam String email,
			@RequestParam String phone,
			@RegisteredOAuth2AuthorizedClient("admin-portal") OAuth2AuthorizedClient authorizedClient) {
		String accessToken = authorizedClient.getAccessToken().getTokenValue();
		AuthAdminApiClient.ApiCallResult authResult = authAdminApiClient.updateOwnProfile(accessToken, displayName,
				email, phone);
		if (!authResult.success()) {
			return "redirect:/admin?profileFailed=1";
		}
		AdminMemberView.Member member = findMemberBySubject(accessToken, oidcUser.getSubject());
		if (member == null) {
			return "redirect:/admin?syncFailed=1";
		}
		ApiCallResult detail = memberAdminApiClient.getMember(accessToken, member.id());
		if (!detail.success()) {
			return "redirect:/admin?syncFailed=1";
		}
		try {
			long version = objectMapper.readTree(detail.body()).get("version").asLong();
			ApiCallResult projection = memberAdminApiClient.updateProfile(
					accessToken,
					member.id(),
					version,
					displayName,
					email);
			return projection.success()
					? "redirect:/admin?profileUpdated=1"
					: "redirect:/admin?syncFailed=1";
		} catch (Exception ex) {
			return "redirect:/admin?syncFailed=1";
		}
	}

	@PostMapping("/admin/members/{memberId}/sensitive/{field}/reveal")
	@ResponseBody
	public ResponseEntity<String> revealSensitiveField(
			@PathVariable long memberId,
			@PathVariable String field,
			@RegisteredOAuth2AuthorizedClient("admin-portal") OAuth2AuthorizedClient authorizedClient) {
		String normalizedField = switch (field) {
			case "EMAIL" -> "EMAIL";
			case "DISPLAY_NAME" -> "DISPLAY_NAME";
			default -> null;
		};
		if (normalizedField == null) {
			return noStoreResponse(HttpStatus.BAD_REQUEST, "{\"message\":\"지원하지 않는 필드입니다.\"}");
		}

		ApiCallResult result = memberAdminApiClient.revealSensitiveField(
				authorizedClient.getAccessToken().getTokenValue(),
				memberId,
				normalizedField);
		if (!result.success()) {
			HttpStatus status = result.statusCode() == 403 ? HttpStatus.FORBIDDEN : HttpStatus.BAD_GATEWAY;
			return noStoreResponse(status, "{\"message\":\"민감정보를 불러오지 못했습니다.\"}");
		}
		return noStoreResponse(HttpStatus.OK, result.body());
	}

	private AdminMemberView.Page parseMembers(ApiCallResult apiResult) {
		if (!apiResult.success() || apiResult.body().isBlank()) {
			return AdminMemberView.Page.empty();
		}
		try {
			return objectMapper.readValue(apiResult.body(), AdminMemberView.Page.class);
		} catch (Exception ex) {
			return AdminMemberView.Page.empty();
		}
	}

	private Map<String, AdminMemberView.AuthUser> parseAuthUsers(
			AuthAdminApiClient.ApiCallResult apiResult) {
		if (!apiResult.success() || apiResult.body().isBlank()) {
			return Map.of();
		}
		try {
			return Arrays.stream(objectMapper.readValue(
					apiResult.body(),
					AdminMemberView.AuthUser[].class))
					.collect(Collectors.toMap(
							AdminMemberView.AuthUser::username,
							Function.identity()));
		} catch (Exception ex) {
			return Map.of();
		}
	}

	private AdminMemberView.AuthUser parseAuthUser(
			AuthAdminApiClient.ApiCallResult apiResult) {
		if (!apiResult.success() || apiResult.body().isBlank()) {
			return null;
		}
		try {
			var node = objectMapper.readTree(apiResult.body());
			return new AdminMemberView.AuthUser(
					node.get("id") == null ? null : node.get("id").asLong(),
					jsonText(node, "username"),
					jsonText(node, "email"),
					jsonText(node, "displayName"),
					jsonText(node, "phone"),
					jsonText(node, "tenantId"),
					node.get("enabled") != null && node.get("enabled").asBoolean(),
					node.get("accountNonLocked") != null
							&& node.get("accountNonLocked").asBoolean(),
					jsonText(node, "status"),
					Set.of(),
					null);
		}
		catch (Exception ex) {
			log.warn("내 정보 응답 변환 실패: {}", ex.getMessage());
			return null;
		}
	}

	private static String jsonText(tools.jackson.databind.JsonNode node, String field) {
		return node.get(field) == null || node.get(field).isNull()
				? ""
				: node.get(field).stringValue();
	}

	private static AdminMemberView.Page enrichMembers(
			AdminMemberView.Page members,
			Map<String, AdminMemberView.AuthUser> accounts) {
		return new AdminMemberView.Page(
				members.content().stream()
						.map(member -> {
							AdminMemberView.AuthUser account = accounts.get(member.userSubject());
							return account == null ? member : member.withAccount(account);
						})
						.toList(),
				members.page(),
				members.size(),
				members.totalElements(),
				members.totalPages());
	}

	private static AdminMemberView.AuthUser findOwnProfile(
			Map<String, AdminMemberView.AuthUser> accounts,
			OidcUser oidcUser) {
		AdminMemberView.AuthUser bySubject = accounts.get(oidcUser.getSubject());
		if (bySubject != null) {
			return bySubject;
		}
		return accounts.values().stream()
				.filter(account -> oidcUser.getEmail() != null
						&& oidcUser.getEmail().equalsIgnoreCase(account.email()))
				.findFirst()
				.orElseGet(() -> new AdminMemberView.AuthUser(
						null,
						oidcUser.getSubject(),
						oidcUser.getEmail(),
						oidcUser.getFullName() == null
								? oidcUser.getSubject()
								: oidcUser.getFullName(),
						"",
						null,
						true,
						true,
						"ACTIVE",
						Set.of("ADMIN"),
						null));
	}

	private AdminMemberView.Member findMember(String accessToken, long memberId, String username) {
		return parseMembers(memberAdminApiClient.listMembers(accessToken)).content().stream()
				.filter(member -> member.id() == memberId && username.equals(member.userSubject()))
				.findFirst()
				.orElse(null);
	}

	private AdminMemberView.Member findMemberBySubject(String accessToken, String username) {
		return parseMembers(memberAdminApiClient.listMembers(accessToken)).content().stream()
				.filter(member -> username.equals(member.userSubject()))
				.findFirst()
				.orElse(null);
	}

	private static String normalizeStatus(String status) {
		return switch (status) {
			case "ACTIVE" -> "ACTIVE";
			case "SUSPENDED" -> "SUSPENDED";
			case "WITHDRAWN" -> "WITHDRAWN";
			case "DELETED" -> "DELETED";
			default -> null;
		};
	}

	private static ResponseEntity<String> noStoreResponse(HttpStatus status, String body) {
		return ResponseEntity.status(status)
				.contentType(MediaType.APPLICATION_JSON)
				.cacheControl(CacheControl.noStore())
				.header("Pragma", "no-cache")
				.header("Referrer-Policy", "no-referrer")
				.body(body);
	}

	public static String maskToken(String token) {
		if (token == null || token.length() < 16) {
			return "***";
		}
		return token.substring(0, 8) + "..." + token.substring(token.length() - 6);
	}
}
