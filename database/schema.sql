

-- Money Transfer System Database Schema
-- MySQL 8.x

-- Drop existing tables (if any)
DROP TABLE IF EXISTS transaction_logs;
DROP TABLE IF EXISTS accounts;

-- Create Accounts Table
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    holder_name VARCHAR(255) NOT NULL,
    balance DECIMAL(18, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT chk_balance CHECK (balance >= 0),
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'LOCKED', 'CLOSED')),
    INDEX idx_holder_name (holder_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create Transaction Logs Table
CREATE TABLE transaction_logs (
    id VARCHAR(36) PRIMARY KEY,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    amount DECIMAL(18, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255),
    idempotency_key VARCHAR(100) NOT NULL UNIQUE,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_from_account FOREIGN KEY (from_account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_to_account FOREIGN KEY (to_account_id) 
        REFERENCES accounts(id) ON DELETE RESTRICT,
    CONSTRAINT chk_amount CHECK (amount > 0),
    CONSTRAINT chk_txn_status CHECK (status IN ('SUCCESS', 'FAILURE')),
    CONSTRAINT chk_different_accounts CHECK (from_account_id != to_account_id),
    
    INDEX idx_idempotency_key (idempotency_key),
    INDEX idx_from_account (from_account_id),
    INDEX idx_to_account (to_account_id),
    INDEX idx_created_on (created_on),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments to tables
ALTER TABLE accounts COMMENT = 'Bank accounts for money transfer system';
ALTER TABLE transaction_logs COMMENT = 'Complete audit trail of all money transfers';