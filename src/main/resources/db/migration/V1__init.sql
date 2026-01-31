CREATE TABLE IF NOT EXISTS transactions (
  id BIGSERIAL PRIMARY KEY,
  transaction_id VARCHAR(64) NOT NULL UNIQUE,
  customer_id VARCHAR(64) NOT NULL,
  amount NUMERIC(18,2) NOT NULL,
  currency VARCHAR(8) NOT NULL,
  merchant VARCHAR(128),
  category VARCHAR(64),
  event_time TIMESTAMPTZ NOT NULL,
  raw_payload JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS fraud_cases (
  case_id UUID PRIMARY KEY,
  transaction_id VARCHAR(64) NOT NULL UNIQUE REFERENCES transactions(transaction_id),
  flagged BOOLEAN NOT NULL,
  risk_score INT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS rule_hits (
  hit_id UUID PRIMARY KEY,
  case_id UUID NOT NULL REFERENCES fraud_cases(case_id) ON DELETE CASCADE,
  rule_id VARCHAR(64) NOT NULL,
  rule_version VARCHAR(16) NOT NULL,
  severity VARCHAR(16) NOT NULL,
  reason TEXT NOT NULL,
  metadata JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
