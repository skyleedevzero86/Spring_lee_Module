package com.sleekydz86.oidstudy.oidc.domain.user.policy;

import com.sleekydz86.oidstudy.oidc.domain.user.UserAccount;

public interface BootstrapAdministratorPolicy {

    boolean shouldBootstrap(UserAccount account);
}