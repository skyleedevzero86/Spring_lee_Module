package com.sleekydz86.oidstudy.oidc.domain.policy;

import com.sleekydz86.oidstudy.oidc.domain.UserAccount;

public interface BootstrapAdministratorPolicy {

    boolean shouldBootstrap(UserAccount account);
}