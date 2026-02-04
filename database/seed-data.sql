-- Money Transfer System - Seed Data
-- Run after schema.sql

USE money_transfer;

INSERT INTO accounts (holder_name, balance, status, version) VALUES
('Alice Johnson', 10000.00, 'ACTIVE', 0),
('Bob Smith', 5000.00, 'ACTIVE', 0),
('Carol Williams', 7500.00, 'ACTIVE', 0),
('David Brown', 2500.00, 'ACTIVE', 0),
('Eve Davis', 15000.00, 'ACTIVE', 0);