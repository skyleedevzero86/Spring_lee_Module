package com.sleekydz86.oidstudy.oidc.web;

import java.util.List;
import com.sleekydz86.oidstudy.oidc.application.user.UserAccountApplicationService;
import com.sleekydz86.oidstudy.oidc.web.factory.AdminNotificationResponseFactory;
import com.sleekydz86.oidstudy.oidc.web.resp.AdminNotificationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final UserAccountApplicationService userAccountApplicationService;
    private final AdminNotificationResponseFactory adminNotificationResponseFactory;

    public AdminNotificationController(
            UserAccountApplicationService userAccountApplicationService,
            AdminNotificationResponseFactory adminNotificationResponseFactory
    ) {
        this.userAccountApplicationService = userAccountApplicationService;
        this.adminNotificationResponseFactory = adminNotificationResponseFactory;
    }

    @GetMapping
    public List<AdminNotificationResponse> notifications(@RequestParam(name = "limit", defaultValue = "10") int limit) {
        return userAccountApplicationService.findRecentNotifications(limit).stream()
                .map(adminNotificationResponseFactory::create)
                .toList();
    }
}