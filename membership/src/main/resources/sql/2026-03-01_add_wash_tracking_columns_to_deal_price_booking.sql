ALTER TABLE carwash_deal_price_booking
    ADD COLUMN IF NOT EXISTS total_washes INTEGER;

ALTER TABLE carwash_deal_price_booking
    ADD COLUMN IF NOT EXISTS used_washes INTEGER;

ALTER TABLE carwash_deal_price_booking
    ADD COLUMN IF NOT EXISTS left_washes INTEGER;

UPDATE carwash_deal_price_booking
SET total_washes = COALESCE(total_washes, 3)
WHERE total_washes IS NULL OR total_washes <= 0;

UPDATE carwash_deal_price_booking
SET used_washes = COALESCE(used_washes, 0)
WHERE used_washes IS NULL OR used_washes < 0;

UPDATE carwash_deal_price_booking
SET left_washes = GREATEST(total_washes - used_washes, 0)
WHERE left_washes IS NULL OR left_washes < 0;

ALTER TABLE carwash_deal_price_booking
    ALTER COLUMN total_washes SET DEFAULT 3;

ALTER TABLE carwash_deal_price_booking
    ALTER COLUMN used_washes SET DEFAULT 0;

ALTER TABLE carwash_deal_price_booking
    ALTER COLUMN left_washes SET DEFAULT 3;