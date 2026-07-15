package com.sleekydz86.loginstudy.auth.web;

import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConsentController {

	private final RegisteredClientRepository registeredClientRepository;

	public ConsentController(RegisteredClientRepository registeredClientRepository) {
		this.registeredClientRepository = registeredClientRepository;
	}

	@GetMapping("/oauth2/consent")
	public String consent(
			Principal principal,
			Model model,
			@RequestParam(OAuth2ParameterNames.CLIENT_ID) String clientId,
			@RequestParam(OAuth2ParameterNames.SCOPE) String scope,
			@RequestParam(OAuth2ParameterNames.STATE) String state) {

		RegisteredClient registeredClient = registeredClientRepository.findByClientId(clientId);
		if (registeredClient == null) {
			throw new IllegalArgumentException("Unknown client_id: " + clientId);
		}

		Set<String> scopesToApprove = new LinkedHashSet<>();
		Set<String> previouslyApproved = new LinkedHashSet<>();
		for (String requestedScope : StringUtils.delimitedListToStringArray(scope, " ")) {
			if (OidcScopes.OPENID.equals(requestedScope)) {
				continue;
			}
			scopesToApprove.add(requestedScope);
		}

		model.addAttribute("clientId", clientId);
		model.addAttribute("clientName", registeredClient.getClientName());
		model.addAttribute("state", state);
		model.addAttribute("scopes", scopesToApprove);
		model.addAttribute("previouslyApprovedScopes", previouslyApproved);
		model.addAttribute("principalName", principal.getName());
		return "consent";
	}
}
