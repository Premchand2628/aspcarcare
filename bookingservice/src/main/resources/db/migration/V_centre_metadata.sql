-- =====================================================================
-- carwash_service_centre — add centre_code (business id) + locality cols
-- Safe to re-run.
-- =====================================================================

ALTER TABLE carwash_service_centre
    ADD COLUMN IF NOT EXISTS centre_code VARCHAR(40),
    ADD COLUMN IF NOT EXISTS pincode     VARCHAR(10),
    ADD COLUMN IF NOT EXISTS city        VARCHAR(80),
    ADD COLUMN IF NOT EXISTS state       VARCHAR(80);

-- Backfill any existing rows so we can later make centre_code NOT NULL+UNIQUE
UPDATE carwash_service_centre
   SET centre_code = CONCAT('LEG-', id)
 WHERE centre_code IS NULL;

-- Enforce uniqueness (does nothing if already created)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_centre_code'
    ) THEN
        ALTER TABLE carwash_service_centre
            ADD CONSTRAINT uq_centre_code UNIQUE (centre_code);
    END IF;
END$$;

-- Helpful filter index
CREATE INDEX IF NOT EXISTS idx_centre_pincode ON carwash_service_centre(pincode);
