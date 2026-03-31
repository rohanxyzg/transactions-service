CREATE TABLE IF NOT EXISTS accounts (
    account_id      BIGSERIAL       PRIMARY KEY,
    document_number VARCHAR(20)     NOT NULL,
    CONSTRAINT uk_accounts_document_number UNIQUE (document_number)
);

CREATE TABLE IF NOT EXISTS operation_types (
    operation_type_id   BIGINT      PRIMARY KEY,
    description         VARCHAR(50) NOT NULL,
    is_credit           BOOLEAN     NOT NULL
);

CREATE TABLE IF NOT EXISTS transactions (
    transaction_id      BIGSERIAL       PRIMARY KEY,
    account_id          BIGINT          NOT NULL REFERENCES accounts(account_id),
    operation_type_id   BIGINT          NOT NULL REFERENCES operation_types(operation_type_id),
    amount              NUMERIC(19, 2)  NOT NULL,
    event_date          TIMESTAMPTZ     NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_account_id ON transactions(account_id);
