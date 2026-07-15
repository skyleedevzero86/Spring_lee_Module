-- Performance / lookup indexes for Authorization Server persistence
CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_registered_client_id
    ON oauth2_authorization (registered_client_id);

CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_principal_name
    ON oauth2_authorization (principal_name);

CREATE INDEX IF NOT EXISTS idx_oauth2_authorization_consent_principal
    ON oauth2_authorization_consent (principal_name);

CREATE INDEX IF NOT EXISTS idx_users_tenant_id
    ON users (tenant_id);

CREATE INDEX IF NOT EXISTS idx_login_history_success_created_at
    ON login_history (success, created_at DESC);
