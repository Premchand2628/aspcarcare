CREATE TABLE IF NOT EXISTS carwash_deal_price_booking (
    id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(20) NOT NULL,
    car_type VARCHAR(40) NOT NULL,
    service_type VARCHAR(40) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    refund_amount NUMERIC(12,2) DEFAULT 0,
    refund_initiated_at TIMESTAMP NULL,
    refund_status VARCHAR(30) DEFAULT 'NOT_INITIATED',
    transaction_id VARCHAR(80) NOT NULL UNIQUE,
    discount_percent_applied NUMERIC(6,2) DEFAULT 0,
    original_amount NUMERIC(12,2) DEFAULT 0,
    payable_amount NUMERIC(12,2) DEFAULT 0,
    wash_type VARCHAR(80) NOT NULL,
    water_provided CHAR(1) NOT NULL,
    plan_type_code VARCHAR(40) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_deal_price_booking_phone
    ON carwash_deal_price_booking (phone);

CREATE INDEX IF NOT EXISTS idx_deal_price_booking_created_at
    ON carwash_deal_price_booking (created_at DESC);
