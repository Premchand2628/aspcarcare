-- =============================================================
-- Add base_price column to carwash_service_centre.
-- Created: 2026-05-03
-- Apply manually:
--   psql "$DB_URL" -f bookingservice/src/main/resources/sql/2026-05-03_add_base_price_to_service_centre.sql
-- =============================================================

ALTER TABLE carwash_service_centre
    ADD COLUMN IF NOT EXISTS base_price NUMERIC(10,2);
