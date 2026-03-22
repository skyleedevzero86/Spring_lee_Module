package com.sleekydz86.oidstudy.oidc.web;

import com.sleekydz86.oidstudy.global.security.AppOidcUser;
import com.sleekydz86.oidstudy.oidc.application.user.CompleteRegistrationCommand;
import com.sleekydz86.oidstudy.oidc.application.user.LoginIdCheckResult;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;
import com.sleekydz86.oidstudy.oidc.web.factory.SessionResponseFactory;
import com.sleekydz86.oidstudy.oidc.web.req.RegistrationRequest;
import com.sleekydz86.oidstudy.oidc.web.resp.LoginIdAvailabilityResponse;
import com.sleekydz86.oidstudy.oidc.web.resp.SessionResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final UserAccountApplicationService userAccountApplicationService;
    private final SessionResponseFactory sessionResponseFactory;

    public RegistrationController(
            UserAccountApplicationService userAccountApplicationService,
            SessionResponseFactory sessionResponseFactory
    ) {
        this.userAccountApplicationService = userAccountApplicationService;
        this.sessionResponseFactory = sessionResponseFactory;
    }

    @GetMapping("/login-id-check")
    public LoginIdAvailabilityResponse checkLoginId(@RequestParam String loginId, Authentication authentication) {
        AppOidcUser principal = (AppOidcUser) authentication.getPrincipal();
        LoginIdCheckResult result = userAccountApplicationService.checkLoginId(loginId, principal.getAccount().getId());
        return new LoginIdAvailabilityResponse(result.available(), result.status(), result.message());
    }

    @PostMapping("/complete")
    public SessionResponse completeRegistration(@Valid @RequestBody RegistrationRequest request, Authentication authentication) {
        AppOidcUser principal = (AppOidcUser) authentication.getPrincipal();
        UserAccount account = userAccountApplicationService.completeRegistration(
                principal.getAccount().getId(),
                new CompleteRegistrationCommand(
                        request.loginId(),
                        request.displayName(),
                        request.contactNumber(),
                        request.agreedToTerms()
                )
        );
        return sessionResponseFactory.create(principal, account);
    }
}