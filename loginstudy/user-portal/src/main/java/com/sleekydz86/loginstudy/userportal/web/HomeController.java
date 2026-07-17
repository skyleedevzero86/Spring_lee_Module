package com.sleekydz86.loginstudy.userportal.web;

import com.sleekydz86.loginstudy.userportal.service.AuthAccountApiClient;
import com.sleekydz86.loginstudy.userportal.service.MemberApiClient;
import com.sleekydz86.loginstudy.userportal.service.MemberApiClient.MemberApiCallResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tools.jackson.databind.ObjectMapper;

@Controller
@Tag(name = "User Portal Pages", description = "OIDC 사용자 포털 화면")
public class HomeController {

	private final MemberApiClient memberApiClient;
	private final AuthAccountApiClient authAccountApiClient;
	private final ObjectMapper objectMapper;

	public HomeController(
			MemberApiClient memberApiClient,
			AuthAccountApiClient authAccountApiClient,
			ObjectMapper objectMapper) {
		this.memberApiClient = memberApiClient;
		this.authAccountApiClient = authAccountApiClient;
		this.objectMapper = objectMapper;
	}

	@GetMapping("/home")
	@Operation(summary = "홈(내 프로필)", description = "OIDC 로그인 후 Access Token으로 member-service /api/members/me 호출")
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
		MemberProfileView profile = parseProfile(apiResult);
		model.addAttribute("profile", profile);
		AuthProfileView authProfile = parseAuthProfile(
				authAccountApiClient.getOwnProfile(accessTokenValue));
		model.addAttribute("authProfile", authProfile == null
				? fallbackProfile(oidcUser, profile)
				: authProfile);

		return "home";
	}

	@PostMapping("/profile")
	@Operation(summary = "내 프로필 수정", description = "현재 로그인 사용자의 이름·이메일·전화번호를 수정합니다.")
	public String updateProfile(
			@RequestParam String displayName,
			@RequestParam String email,
			@RequestParam String phone,
			@RegisteredOAuth2AuthorizedClient("user-portal") OAuth2AuthorizedClient authorizedClient) {
		String normalizedName = displayName == null ? "" : displayName.trim();
		String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
		String normalizedPhone = phone == null ? "" : phone.trim();
		if (normalizedName.isEmpty()
				|| normalizedName.length() > 100
				|| !normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
				|| !normalizedPhone.matches("^[0-9+() -]{7,30}$")) {
			return "redirect:/home?updateFailed=validation";
		}

		String accessToken = authorizedClient.getAccessToken().getTokenValue();
		MemberApiCallResult currentResult = memberApiClient.fetchMyProfile(accessToken);
		MemberProfileView currentProfile = parseProfile(currentResult);
		if (currentProfile == null || currentProfile.id() == null) {
			return "redirect:/home?updateFailed=profile";
		}

		AuthAccountApiClient.ApiCallResult authResult = authAccountApiClient.updateOwnProfile(
				accessToken,
				normalizedName,
				normalizedEmail,
				normalizedPhone);
		if (!authResult.success()) {
			return "redirect:/home?updateFailed=auth";
		}

		try {
			String requestBody = objectMapper.writeValueAsString(Map.of(
					"version", currentProfile.version(),
					"displayName", normalizedName,
					"email", normalizedEmail));
			MemberApiCallResult updateResult =
					memberApiClient.updateProfile(accessToken, currentProfile.id(), requestBody);
			return updateResult.success()
					? "redirect:/home?updated=1"
					: "redirect:/home?syncFailed=1";
		}
		catch (Exception ex) {
			return "redirect:/home?updateFailed=api";
		}
	}

	private AuthProfileView parseAuthProfile(AuthAccountApiClient.ApiCallResult apiResult) {
		if (!apiResult.success() || apiResult.body().isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(apiResult.body(), AuthProfileView.class);
		}
		catch (Exception ex) {
			return null;
		}
	}

	private static AuthProfileView fallbackProfile(
			OidcUser oidcUser,
			MemberProfileView profile) {
		String displayName = profile == null || profile.displayName() == null
				? oidcUser.getSubject()
				: profile.displayName();
		String email = profile == null || profile.email() == null
				? oidcUser.getEmail()
				: profile.email();
		return new AuthProfileView(
				null,
				oidcUser.getSubject(),
				email,
				displayName,
				"",
				null,
				true,
				true,
				"ACTIVE",
				Set.of("USER"),
				null);
	}

	private MemberProfileView parseProfile(MemberApiCallResult apiResult) {
		if (!apiResult.success() || apiResult.body().isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(apiResult.body(), MemberProfileView.class);
		}
		catch (Exception ex) {
			return null;
		}
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
