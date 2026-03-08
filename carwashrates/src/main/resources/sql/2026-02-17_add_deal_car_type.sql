ALTER TABLE carwash_deal_prices
ADD COLUMN IF NOT EXISTS deal_car_type VARCHAR(50);

UPDATE carwash_deal_prices
SET deal_car_type = CASE
  WHEN id BETWEEN 1 AND 12 THEN 'Hatchback'
  WHEN id BETWEEN 13 AND 24 THEN 'Sedan'
  WHEN id BETWEEN 25 AND 36 THEN 'SUV'
  WHEN id BETWEEN 37 AND 48 THEN 'MPV'
  WHEN id BETWEEN 49 AND 60 THEN 'Pickup'
  ELSE deal_car_type
END
WHERE deal_car_type IS NULL OR TRIM(deal_car_type) = '';

ALTER TABLE carwash_deal_prices
ALTER COLUMN deal_car_type SET NOT NULL;

CREATE INDEX IF NOT EXISTS idx_deal_prices_car_type
ON carwash_deal_prices (deal_car_type);
