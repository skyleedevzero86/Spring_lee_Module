INSERT INTO member_profile (
    user_subject,
    email,
    display_name,
    status,
    tenant_id,
    version,
    joined_at,
    updated_at
)
SELECT
    'seed-' || g,
    'user' || g || '@seed.loginstudy.local',
    'Seed User ' || lpad(g::text, 6, '0'),
    CASE
        WHEN g % 20 = 0 THEN 'SUSPENDED'
        WHEN g % 15 = 0 THEN 'INACTIVE'
        ELSE 'ACTIVE'
    END,
    'tenant-demo',
    0,
    TIMESTAMPTZ '2024-01-01 00:00:00+00' + ((g % 500000) * INTERVAL '1 minute'),
    NOW()
FROM generate_series(1, 100000) AS g
ON CONFLICT DO NOTHING;
