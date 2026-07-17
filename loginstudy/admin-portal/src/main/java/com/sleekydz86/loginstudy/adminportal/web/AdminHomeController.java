package com.sleekydz86.loginstudy.adminportal.web;

import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient.ApiCallResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.ResponseBody;
import tools.jackson.databind.ObjectMapper;

@Controller
@Tag(name = "Admin Portal Pages", description = "관리자 OIDC 포털 화면")
public class AdminHomeController {

	private final MemberAdminApiClient memberAdminApiClient;
	private final ObjectMapper objectMapper;

	public AdminHomeController(MemberAdminApiClient memberAdminApiClient, ObjectMapper objectMapper) {
		this.memberAdminApiClient = memberAdminApiClient;
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
		model.addAttribute("members", parseMembers(apiResult));
		return "admin";
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
		}
		catch (Exception ex) {
			return AdminMemberView.Page.empty();
		}
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
