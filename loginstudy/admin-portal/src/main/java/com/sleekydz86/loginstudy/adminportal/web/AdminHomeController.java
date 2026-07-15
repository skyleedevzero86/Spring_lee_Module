package com.sleekydz86.loginstudy.adminportal.web;

import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient;
import com.sleekydz86.loginstudy.adminportal.service.MemberAdminApiClient.ApiCallResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Tag(name = "Admin Portal Pages", description = "관리자 OIDC 포털 화면")
public class AdminHomeController {

	private final MemberAdminApiClient memberAdminApiClient;

	public AdminHomeController(MemberAdminApiClient memberAdminApiClient) {
		this.memberAdminApiClient = memberAdminApiClient;
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
		model.addAttribute("memberApiBody", apiResult.body());
		return "admin";
	}

	public static String maskToken(String token) {
		if (token == null || token.length() < 16) {
			return "***";
		}
		return token.substring(0, 8) + "..." + token.substring(token.length() - 6);
	}
}
