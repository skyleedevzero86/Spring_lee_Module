package com.sleekydz86.oidstudy.oidc.domain.policy;

import java.util.Locale;
import com.sleekydz86.oidstudy.global.config.AppSecurityProperties;
import com.sleekydz86.oidstudy.oidc.domain.UserAccount;
import org.springframework.stereotype.Component;

@Component
public class PropertyBasedBootstrapAdministratorPolicy implements BootstrapAdministratorPolicy {

    private final AppSecurityProperties properties;

    public PropertyBasedBootstrapAdministratorPolicy(AppSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean shouldBootstrap(UserAccount account) {
        if (account.isAdmin()) {
            return false;
        }
        String configuredSub = blankToNull(properties.bootstrapAdminSub());
        String configuredEmail = normalizeEmail(properties.bootstrapAdminEmail());
        return (configuredSub != null && configuredSub.equals(account.getProviderUserId()))
                || (configuredEmail != null && configuredEmail.equals(normalizeEmail(account.getEmail())));
    }

    private String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeEmail(String value) {
        String normalized = blankToNull(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}