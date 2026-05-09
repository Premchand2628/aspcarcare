-- Aligns the pre-existing carwash_centre_rate with the override resolver model.
-- Safe to re-run: every operation is IF NOT EXISTS / NOT VALID guarded.

ALTER TABLE carwash_centre_rate
    ADD COLUMN IF NOT EXISTS currency      VARCHAR(10)  NOT NULL DEFAULT 'INR',
    ADD COLUMN IF NOT EXISTS effective_to  TIMESTAMP    NULL,
    ADD COLUMN IF NOT EXISTS created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at    TIMESTAMP    NOT NULL DEFAULT NOW();

-- Make sure service_mode has a default so legacy inserts don't break.
ALTER TABLE carwash_centre_rate
    ALTER COLUMN service_mode SET DEFAULT 'IN_STORE';

-- Backfill nulls (no-op if already populated).
UPDATE carwash_centre_rate SET service_mode = 'IN_STORE' WHERE service_mode IS NULL;

-- Ensure NOT NULL on service_mode (idempotent).
ALTER TABLE carwash_centre_rate
    ALTER COLUMN service_mode SET NOT NULL;

-- Lookup index for the resolver.
CREATE INDEX IF NOT EXISTS idx_centre_rate_lookup
    ON carwash_centre_rate (centre_id, car_type, wash_type, active);
