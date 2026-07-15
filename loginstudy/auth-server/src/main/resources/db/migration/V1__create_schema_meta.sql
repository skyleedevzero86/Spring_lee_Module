CREATE TABLE schema_meta (
    id              BIGSERIAL PRIMARY KEY,
    service_name    VARCHAR(64)  NOT NULL,
    schema_version  VARCHAR(32)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_schema_meta_service_name UNIQUE (service_name)
);

INSERT INTO schema_meta (service_name, schema_version)
VALUES ('auth-server', 'V1');

COMMENT ON TABLE schema_meta IS 'Bootstrap marker for auth-server DB ownership and Flyway connectivity checks';
