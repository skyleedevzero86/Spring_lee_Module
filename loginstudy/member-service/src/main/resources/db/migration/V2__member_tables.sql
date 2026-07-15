CREATE TABLE member_profile (
    id              BIGSERIAL PRIMARY KEY,
    user_subject    VARCHAR(100)  NOT NULL,
    email           VARCHAR(255)  NOT NULL,
    display_name    VARCHAR(100)  NOT NULL,
    status          VARCHAR(32)   NOT NULL,
    tenant_id       VARCHAR(64)   NOT NULL,
    version         BIGINT        NOT NULL DEFAULT 0,
    joined_at       TIMESTAMPTZ   NOT NULL,
    updated_at      TIMESTAMPTZ   NOT NULL,
    CONSTRAINT uk_member_profile_user_subject UNIQUE (user_subject),
    CONSTRAINT uk_member_profile_email UNIQUE (email)
);

CREATE TABLE member_address (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT        NOT NULL,
    country_code    VARCHAR(2)    NOT NULL,
    city            VARCHAR(100)  NOT NULL,
    street_line     VARCHAR(255)  NOT NULL,
    postal_code     VARCHAR(32)   NOT NULL,
    CONSTRAINT fk_member_address_member
        FOREIGN KEY (member_id) REFERENCES member_profile (id) ON DELETE CASCADE
);

CREATE TABLE member_preferences (
    id                  BIGSERIAL PRIMARY KEY,
    member_id           BIGINT       NOT NULL,
    marketing_opt_in    BOOLEAN      NOT NULL DEFAULT FALSE,
    locale              VARCHAR(16)  NOT NULL DEFAULT 'ko-KR',
    timezone            VARCHAR(64)  NOT NULL DEFAULT 'Asia/Seoul',
    CONSTRAINT uk_member_preferences_member UNIQUE (member_id),
    CONSTRAINT fk_member_preferences_member
        FOREIGN KEY (member_id) REFERENCES member_profile (id) ON DELETE CASCADE
);

CREATE TABLE member_status_history (
    id              BIGSERIAL PRIMARY KEY,
    member_id       BIGINT       NOT NULL,
    from_status     VARCHAR(32),
    to_status       VARCHAR(32)  NOT NULL,
    changed_by      VARCHAR(100) NOT NULL,
    reason          VARCHAR(255),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_member_status_history_member
        FOREIGN KEY (member_id) REFERENCES member_profile (id) ON DELETE CASCADE
);

CREATE INDEX idx_member_profile_status_joined_at ON member_profile (status, joined_at DESC);
CREATE INDEX idx_member_profile_display_name ON member_profile (display_name);
CREATE INDEX idx_member_profile_tenant_status ON member_profile (tenant_id, status);
CREATE INDEX idx_member_status_history_member_created ON member_status_history (member_id, created_at DESC);
