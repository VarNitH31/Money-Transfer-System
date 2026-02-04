-- Money Transfer System - Database Schema
-- MySQL 8.x

CREATE DATABASE IF NOT EXISTS money_transfer;
USE money_transfer;

-- Accounts table
CREATE TABLE accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    holder_name VARCHAR(255) NOT NULL,
    balance DECIMAL(18,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version INT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Transaction logs table
CREATE TABLE transaction_logs (
    id VARCHAR(36) PRIMARY KEY,
    from_account BIGINT NOT NULL,
    to_account BIGINT NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(255) NULL,
    idempotency_key VARCHAR(100) UNIQUE,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_account) REFERENCES accounts(id),
    FOREIGN KEY (to_account) REFERENCES accounts(id)
);

CREATE INDEX idx_transaction_from ON transaction_logs(from_account);
CREATE INDEX idx_transaction_to ON transaction_logs(to_account);
CREATE INDEX idx_transaction_created ON transaction_logs(created_on);
CREATE INDEX idx_transaction_idempotency ON transaction_logs(idempotency_key);