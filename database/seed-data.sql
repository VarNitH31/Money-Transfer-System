-- Seed Data for Money Transfer System
-- Test accounts with initial balances

-- Clear existing data (optional - use with caution)
-- DELETE FROM transaction_logs;
-- DELETE FROM accounts;

-- Reset auto-increment (optional)
-- ALTER TABLE accounts AUTO_INCREMENT = 1;

-- Insert Test Accounts
INSERT INTO accounts (holder_name, balance, status) VALUES
('Alice Johnson', 10000.00, 'ACTIVE'),
('Bob Smith', 5000.00, 'ACTIVE'),
('Charlie Brown', 15000.00, 'ACTIVE'),
('Diana Prince', 7500.00, 'ACTIVE'),
('Eve Adams', 3000.00, 'ACTIVE'),
('Frank Miller', 20000.00, 'ACTIVE'),
('Grace Lee', 12000.00, 'ACTIVE'),
('Henry Wilson', 8000.00, 'ACTIVE'),
('Ivy Chen', 500.00, 'LOCKED'),
('Jack Davis', 0.00, 'CLOSED');