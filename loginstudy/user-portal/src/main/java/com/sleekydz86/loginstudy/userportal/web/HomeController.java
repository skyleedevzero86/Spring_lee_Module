package com.sleekydz86.loginstudy.userportal.web;

import com.sleekydz86.loginstudy.userportal.service.MemberApiClient;
import com.sleekydz86.loginstudy.userportal.service.MemberApiClient.MemberApiCallResult;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	private final MemberApiClient memberApiClient;

	public HomeController(MemberApiClient memberApiClient) {
		this.memberApiClient = memberApiClient;
	}

	@GetMapping("/home")
	public String home(
			@AuthenticationPrincipal OidcUser oidcUser,
			@RegisteredOAuth2AuthorizedClient("user-portal") OAuth2AuthorizedClient authorizedClient,
			Model model) {

		String accessTokenValue = authorizedClient.getAccessToken().getTokenValue();
		Instant accessTokenExpiresAt = authorizedClient.getAccessToken().getExpiresAt();

		model.addAttribute("subject", oidcUser.getSubject());
		model.addAttribute("fullName", oidcUser.getFullName());
		model.addAttribute("email", oidcUser.getEmail());
		model.addAttribute("claims", oidcUser.getClaims());
		model.addAttribute("idTokenClaims", oidcUser.getIdToken().getClaims());
		model.addAttribute("userInfoClaims", toDisplayMap(oidcUser.getUserInfo() == null
				? Map.of()
				: oidcUser.getUserInfo().getClaims()));
		model.addAttribute("accessTokenMasked", maskToken(accessTokenValue));
		model.addAttribute("accessTokenExpiresAt", accessTokenExpiresAt);
		model.addAttribute("scopes", authorizedClient.getAccessToken().getScopes());
		model.addAttribute("idTokenMasked", maskToken(oidcUser.getIdToken().getTokenValue()));

		MemberApiCallResult apiResult = memberApiClient.fetchMyProfile(accessTokenValue);
		model.addAttribute("memberApiSuccess", apiResult.success());
		model.addAttribute("memberApiStatus", apiResult.statusCode());
		model.addAttribute("memberApiBody", apiResult.body());

		return "home";
	}

	private static Map<String, Object> toDisplayMap(Map<String, Object> source) {
		return new LinkedHashMap<>(source);
	}

	public static String maskToken(String token) {
		if (token == null || token.length() < 16) {
			return "***";
		}
		return token.substring(0, 8) + "..." + token.substring(token.length() - 6);
	}
}
