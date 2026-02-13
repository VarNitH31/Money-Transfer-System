CREATE WAREHOUSE MT_WH 
  WITH 
  WAREHOUSE_SIZE = 'SMALL' 
  AUTO_SUSPEND = 60 
  AUTO_RESUME = TRUE;


CREATE DATABASE MONEY_TRANSFER_DW;

use database money_transfer_dw;

CREATE SCHEMA ANALYTICS;

use warehouse mt_wh;

use schema analytics;

CREATE STAGE my_stage 
  FILE_FORMAT = (TYPE = 'CSV' FIELD_OPTIONALLY_ENCLOSED_BY = '"');

CREATE TABLE DIM_ACCOUNT (
  account_key NUMBER AUTOINCREMENT PRIMARY KEY,
  account_id NUMBER,
  holder_name STRING,
  status STRING,
  effective_date DATE
);

CREATE TABLE DIM_DATE (
  date_key NUMBER AUTOINCREMENT PRIMARY KEY,
  full_date TIMESTAMP_NTZ,
  day INT,
  month INT,
  year INT,
  quarter INT
);

CREATE TABLE FACT_TRANSACTIONS (
  transaction_key NUMBER AUTOINCREMENT PRIMARY KEY,
  account_from_key NUMBER REFERENCES DIM_ACCOUNT(account_key),
  account_to_key NUMBER REFERENCES DIM_ACCOUNT(account_key),
  date_key NUMBER REFERENCES DIM_DATE(date_key) ,
  amount DECIMAL(18,2),
  status STRING
);

COPY INTO DIM_ACCOUNT 
FROM @my_stage/account_data.csv
FILE_FORMAT = (TYPE = 'CSV' FIELD_OPTIONALLY_ENCLOSED_BY = '"');

truncate table if exists DIM_DATE;

drop table if exists DIM_DATE;

COPY INTO DIM_DATE 
FROM @my_stage/date_data.csv
FILE_FORMAT = (TYPE = 'CSV' FIELD_OPTIONALLY_ENCLOSED_BY = '"');

COPY INTO FACT_TRANSACTIONS
FROM @my_stage/transactions_data.csv
FILE_FORMAT = (TYPE = 'CSV' FIELD_OPTIONALLY_ENCLOSED_BY = '"');

select * from dim_account;

SELECT a.holder_name, COUNT(t.transaction_key) AS transaction_count
FROM FACT_TRANSACTIONS t
JOIN DIM_ACCOUNT a ON t.account_from_key = a.account_key
GROUP BY a.holder_name
ORDER BY transaction_count DESC;

SELECT d.full_date, COUNT(t.transaction_key) AS transaction_count, SUM(t.amount) AS total_amount
FROM FACT_TRANSACTIONS t
JOIN DIM_DATE d ON t.date_key = d.date_key
GROUP BY d.full_date;

//Daily Transaction Volume
SELECT
    d.full_date,
    COUNT(ft.transaction_key) AS transaction_count,
    SUM(ft.amount) AS total_amount
FROM FACT_TRANSACTIONS ft
JOIN DIM_DATE d 
    ON ft.date_key = d.date_key
GROUP BY d.full_date
ORDER BY d.full_date;

//Account Activity
SELECT
    a.holder_name,
    a.account_id,
    COUNT(ft.transaction_key) AS total_transactions
FROM FACT_TRANSACTIONS ft
JOIN DIM_ACCOUNT a 
    ON ft.account_from_key = a.account_key
GROUP BY a.holder_name, a.account_id
ORDER BY total_transactions DESC;

//Success Rate
SELECT
    ROUND(
        100.0 * SUM(CASE WHEN ft.status = 'SUCCESS' THEN 1 ELSE 0 END) 
        / COUNT(*), 
    2) AS success_rate_percent
FROM FACT_TRANSACTIONS ft;

//Peak Hours
SELECT
    DATE_PART('HOUR', d.full_date) AS hour_of_day,
    COUNT(ft.transaction_key) AS transaction_count
FROM FACT_TRANSACTIONS ft
JOIN DIM_DATE d
  ON ft.date_key = d.date_key
GROUP BY DATE_PART('HOUR',TO_TIMESTAMP_NTZ(d.full_date))
ORDER BY transaction_count DESC;

SELECT
  EXTRACT(HOUR FROM d.full_date) AS hour_of_day,
  COUNT(ft.transaction_key)      AS transaction_count
FROM FACT_TRANSACTIONS ft
JOIN DIM_DATE d
  ON ft.date_key = d.date_key
GROUP BY EXTRACT(HOUR FROM d.full_date)
ORDER BY transaction_count DESC;

select * from DIM_DATE;

select * from FACT_TRANSACTIONS;

//Avg Transfer Amount
SELECT
    AVG(amount) AS avg_transfer_amount
FROM FACT_TRANSACTIONS;






