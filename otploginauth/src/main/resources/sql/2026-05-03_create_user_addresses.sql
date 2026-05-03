-- =============================================================
-- Saved-address book for carwash users.
-- Created: 2026-05-03
-- Apply manually:
--   psql "$DB_URL" -f otploginauth/src/main/resources/sql/2026-05-03_create_user_addresses.sql
-- =============================================================

CREATE TABLE IF NOT EXISTS user_addresses (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES carwash_user(id) ON DELETE CASCADE,
    label           VARCHAR(20)  NOT NULL DEFAULT 'Home',
    full_name       VARCHAR(120),
    phone           VARCHAR(15),
    zipcode         VARCHAR(10)  NOT NULL,
    area            VARCHAR(150) NOT NULL,
    street_address  VARCHAR(255) NOT NULL,
    city            VARCHAR(80)  NOT NULL,
    state           VARCHAR(80)  NOT NULL,
    landmark        VARCHAR(150),
    latitude        NUMERIC(9,6),
    longitude       NUMERIC(9,6),
    is_default      BOOLEAN      NOT NULL DEFAULT FALSE,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_addresses_user_active
    ON user_addresses(user_id)
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_user_addresses_zipcode
    ON user_addresses(zipcode);

-- At most one default address per user (only counting active rows).
CREATE UNIQUE INDEX IF NOT EXISTS uniq_user_default_address
    ON user_addresses(user_id)
    WHERE is_default = TRUE AND is_active = TRUE;
