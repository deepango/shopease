CREATE TABLE IF NOT EXISTS payments (
    id CHAR(36) NOT NULL PRIMARY KEY,
    order_id CHAR(36) NOT NULL UNIQUE,
    stripe_payment_intent_id VARCHAR(255),
    stripe_client_secret VARCHAR(500),
    status ENUM('PENDING','SUCCESS','FAILED','REFUNDED','RECONCILED') NOT NULL DEFAULT 'PENDING',
    amount DECIMAL(10,2) NOT NULL,
    currency CHAR(3) NOT NULL DEFAULT 'USD',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_payments_order_id (order_id),
    INDEX idx_payments_status (status),
    INDEX idx_payments_stripe_intent (stripe_payment_intent_id),
    INDEX idx_payments_created_at (created_at)
);
