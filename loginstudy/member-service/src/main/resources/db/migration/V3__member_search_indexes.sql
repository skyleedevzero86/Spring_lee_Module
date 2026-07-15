CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_member_profile_joined_at_id
    ON member_profile (joined_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_member_profile_status_joined_at_id
    ON member_profile (status, joined_at DESC, id DESC);

CREATE INDEX IF NOT EXISTS idx_member_profile_email_lower
    ON member_profile (LOWER(email));

CREATE INDEX IF NOT EXISTS idx_member_profile_display_name_trgm
    ON member_profile USING GIN (display_name gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_member_address_member_id
    ON member_address (member_id);

CREATE INDEX IF NOT EXISTS idx_member_preferences_member_id
    ON member_preferences (member_id);
