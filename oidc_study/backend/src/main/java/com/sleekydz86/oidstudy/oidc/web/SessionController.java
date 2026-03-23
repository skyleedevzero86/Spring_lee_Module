package com.sleekydz86.oidstudy.oidc.web;

import com.sleekydz86.oidstudy.global.security.AppOidcUser;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;
import com.sleekydz86.oidstudy.oidc.web.factory.DashboardResponseFactory;
import com.sleekydz86.oidstudy.oidc.web.factory.SessionResponseFactory;
import com.sleekydz86.oidstudy.oidc.web.resp.DashboardResponse;
import com.sleekydz86.oidstudy.oidc.web.resp.SessionResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final UserAccountApplicationService userAccountApplicationService;
    private final SessionResponseFactory sessionResponseFactory;
    private final DashboardResponseFactory dashboardResponseFactory;

    public SessionController(
            UserAccountApplicationService userAccountApplicationService,
            SessionResponseFactory sessionResponseFactory,
            DashboardResponseFactory dashboardResponseFactory
    ) {
        this.userAccountApplicationService = userAccountApplicationService;
        this.sessionResponseFactory = sessionResponseFactory;
        this.dashboardResponseFactory = dashboardResponseFactory;
    }

    @GetMapping("/session")
    public SessionResponse session(Authentication authentication) {
        if (authentication == null) {
            return sessionResponseFactory.anonymous();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof AppOidcUser appOidcUser) {
            UserAccount refreshed = userAccountApplicationService.getRequired(appOidcUser.getAccount().getId());
            return sessionResponseFactory.create(appOidcUser, refreshed);
        }
        if (principal instanceof OAuth2User oauth2User) {
            return sessionResponseFactory.create(oauth2User);
        }
        return sessionResponseFactory.anonymous();
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(Authentication authentication) {
        AppOidcUser principal = (AppOidcUser) authentication.getPrincipal();
        UserAccount refreshed = userAccountApplicationService.getRequired(principal.getAccount().getId());
        return dashboardResponseFactory.create(userAccountApplicationService.buildDashboard(refreshed));
    }
}