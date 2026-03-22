package com.sleekydz86.oidstudy.oidc.web.factory;

import com.sleekydz86.oidstudy.oidc.application.user.DashboardSnapshot;
import com.sleekydz86.oidstudy.oidc.web.resp.DashboardResponse;
import org.springframework.stereotype.Component;

@Component
public class DashboardResponseFactory {

    public DashboardResponse create(DashboardSnapshot snapshot) {
        return new DashboardResponse(
                snapshot.totalUsers(),
                snapshot.activeUsers(),
                snapshot.pendingUsers(),
                snapshot.rejectedUsers(),
                snapshot.withdrawnUsers(),
                snapshot.currentUser().roleSnapshot(),
                snapshot.currentUser().getStatus().name()
        );
    }
}