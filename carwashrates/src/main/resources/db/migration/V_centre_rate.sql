-- Per-centre rate overrides. Sparse: only rows that differ from carwash_rates default.
-- Resolution at read time: COALESCE(centre_override, default_rate).
CREATE TABLE IF NOT EXISTS carwash_centre_rate (
    id              BIGSERIAL PRIMARY KEY,
    centre_id       BIGINT       NOT NULL,
    vehicle_type    VARCHAR(20)  NOT NULL,
    wash_level      VARCHAR(20)  NOT NULL,
    amount          NUMERIC(10,2) NOT NULL,
    currency        VARCHAR(10)  NOT NULL DEFAULT 'INR',
    active          BOOLEAN      NOT NULL DEFAULT TRUE,
    effective_from  TIMESTAMP    NOT NULL DEFAULT NOW(),
    effective_to    TIMESTAMP    NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_centre_rate UNIQUE (centre_id, vehicle_type, wash_level, effective_from)
);

CREATE INDEX IF NOT EXISTS idx_centre_rate_lookup
    ON carwash_centre_rate (centre_id, vehicle_type, wash_level, active);
